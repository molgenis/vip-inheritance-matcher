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
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;

public class VepMapper {

  public static final String GENE = "Gene";
  public static final String SYMBOL_SOURCE = "SYMBOL_SOURCE";
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  private static final String INHERITANCE = "InheritanceModesGene";
  public static final String INCOMPLETE_PENETRANCE_INDEX = "IncompletePenetrance";
  private String vepField = null;
  private int geneIndex = -1;
  private int geneSourceIndex = -1;
  private int inheritanceIndex = -1;
  private int incompletePenetranceIndex = -1;

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
        this.geneIndex = getVepIndex(nestedInfo, GENE);
        this.geneSourceIndex = getVepIndex(nestedInfo, SYMBOL_SOURCE);
        this.incompletePenetranceIndex = nestedInfo.indexOf(INCOMPLETE_PENETRANCE_INDEX);
        this.inheritanceIndex = nestedInfo.indexOf(INHERITANCE);

        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  private int getVepIndex(List<String> nestedInfo, String vepField) {
    int index;
    if (nestedInfo.contains(vepField)) {
      index = nestedInfo.indexOf(vepField);
    } else {
      throw new MissingVepAnnotationException(vepField);
    }
    return index;
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
      String source = vepSplit[geneSourceIndex];
      if(!gene.isEmpty()) {
        if (!knownGenes.containsKey(gene)) {
          Set<InheritanceMode> modes = new HashSet<>();
          if (inheritanceIndex != -1) {
            String[] inheritanceModes
                = vepSplit[inheritanceIndex].split("&");
            mapGeneInheritance(modes, inheritanceModes);
          }
          boolean isIncompletePenetrance = false;
          if (incompletePenetranceIndex != -1) {
            isIncompletePenetrance = vepSplit[incompletePenetranceIndex].equals("1");
          }
          genes.put(gene, new Gene(gene, source, isIncompletePenetrance, modes));
        } else {
          genes.put(gene, knownGenes.get(gene));
        }
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
          //We ignore all the modes that are not used for matching.
      }
    }
  }

  public boolean containsIncompletePenetrance(VariantContext variantContext) {
    Map<String, Gene> genes = getGenes(variantContext);
    return genes.values().stream().anyMatch(Gene::isIncompletePenetrance);
  }
}
