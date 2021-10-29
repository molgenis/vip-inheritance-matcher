package org.molgenis.vcf.inheritance.matcher;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;

public class VepMapper {

  public static final String GENE = "Gene";
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  private static final String INHERITANCE = "InheritanceModesGene";
  private String vepField = null;
  private int geneIndex = -1;
  private int inheritanceIndex = -1;

  public VepMapper(VCFFileReader vcfFileReader) {
    init(vcfFileReader);
  }

  private static boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
  }

  private static List<String> getNestedInfoIds(VCFInfoHeaderLine vcfInfoHeaderLine) {
    String description = vcfInfoHeaderLine.getDescription();
    String[] infoIds = description.substring(INFO_DESCRIPTION_PREFIX.length()).split("\\|", -1);
    return asList(infoIds);
  }

  private void init(VCFFileReader vcfFileReader) {
    VCFHeader vcfHeader = vcfFileReader.getFileHeader();
    for (VCFInfoHeaderLine vcfInfoHeaderLine : vcfHeader.getInfoHeaderLines()) {
      if (canMap(vcfInfoHeaderLine)) {
        this.vepField = vcfInfoHeaderLine.getID();
        List<String> nestedInfo = getNestedInfoIds(vcfInfoHeaderLine);
        if (nestedInfo.contains(GENE)) {
          this.geneIndex = nestedInfo.indexOf(GENE);
        } else {
          throw new MissingVepAnnotationException("GENE");
        }
        this.inheritanceIndex = nestedInfo.indexOf(INHERITANCE);

        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  public Map<String, Gene> getGenes(VariantContext vc) {
    return getGenes(vc, emptyMap());
  }

  public Map<String, Gene> getGenes(VariantContext vc, Map<String, Gene> knownGenes) {
    Map<String, Gene> genes = new HashMap<>();
    List<String> vepValues = vc.getAttributeAsStringList(vepField, "");
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      String gene = vepSplit[geneIndex];
      if (!knownGenes.containsKey(gene)) {
        Set<InheritanceMode> modes = new HashSet<>();
        if(inheritanceIndex != -1) {
          String[] inheritanceModes
              = vepSplit[inheritanceIndex].split("&");
          mapGeneInheritance(modes, inheritanceModes);
        }
        genes.put(gene, new Gene(gene, modes));
      } else {
        genes.put(gene, knownGenes.get(gene));
      }
    }
    return genes;
  }

  private void mapGeneInheritance(Set<InheritanceMode> modes, String[] inheritanceModes) {
    for (String mode : inheritanceModes) {
      switch (mode) {
        case "AR":
          modes.add(InheritanceMode.AR);
          break;
        case "AD":
          modes.add(InheritanceMode.AD);
          break;
        case "XLR":
          modes.add(InheritanceMode.XLR);
          break;
        case "XLD":
          modes.add(InheritanceMode.XLD);
          break;
        case "XL":
          modes.add(InheritanceMode.XLR);
          modes.add(InheritanceMode.XLD);
          break;
        default:
          //We ignore all the modes that are not used for matching (not provided by genmod)
      }
    }
  }

  public Set<String> getNonPenetranceGenesForVariant(VariantContext variantContext, Set<String> lowPenetranceGenes) {
    Set<String> nonPenetranceGenesForVariant = new HashSet<>();
    List<String> genes = getGenes(variantContext).values().stream()
        .map(Gene::getId).collect(
            Collectors.toList());
    for (String gene : genes) {
      if (lowPenetranceGenes.contains(gene)) {
        nonPenetranceGenesForVariant.add(gene);
      }
    }
    return nonPenetranceGenesForVariant;
  }
}
