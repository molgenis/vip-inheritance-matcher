package org.molgenis.vcf.inheritance.matcher;

import static java.util.Arrays.asList;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;

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
        if (nestedInfo.contains(INHERITANCE)) {
          this.inheritanceIndex = nestedInfo.indexOf(INHERITANCE);
        } else {
          throw new MissingVepAnnotationException(INHERITANCE);
        }
        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  public Map<String, Set<InheritanceModeEnum>> getGeneInheritanceMap(VariantContext vc) {
    Map<String, Set<InheritanceModeEnum>> genes = new HashMap<>();
    List<String> vepValues = vc.getAttributeAsStringList(vepField, "");
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      String gene = vepSplit[geneIndex];
      String[] inheritanceModes = vepSplit[inheritanceIndex].split("&");
      Set<InheritanceModeEnum> modes = new HashSet<>();
      for (String mode : inheritanceModes) {
        switch (mode) {
          case "AR":
            modes.add(InheritanceModeEnum.AR);
            break;
          case "AD":
            modes.add(InheritanceModeEnum.AD);
            break;
          case "XLR":
            modes.add(InheritanceModeEnum.XLR);
            break;
          case "XLD":
            modes.add(InheritanceModeEnum.XLD);
            break;
          case "XL":
            modes.add(InheritanceModeEnum.XLD);
            modes.add(InheritanceModeEnum.XLR);
            break;
          default:
            //We ignore all the modes that are not used for matching (not provided by genmod)
        }
      }
      genes.put(gene, modes);
    }
    return genes;
  }

  public Set<String> getGenes(VariantContext vc) {
    Set<String> genes = new HashSet<>();
    List<String> vepValues = vc.getAttributeAsStringList(vepField, "");
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      String gene = vepSplit[geneIndex];
      if (!gene.isEmpty()) {
        genes.add(gene);
      }
    }
    return genes;
  }
}
