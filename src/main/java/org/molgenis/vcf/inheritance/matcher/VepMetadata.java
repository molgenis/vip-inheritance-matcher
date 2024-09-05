package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Map;

import lombok.Getter;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.model.FieldMetadata;
import org.molgenis.vcf.utils.model.NestedField;

public class VepMetadata {

  public static final String GENE = "Gene";
  public static final String ALLELE_NUM = "ALLELE_NUM";
  public static final String VIP_CLASS = "VIPC";
  public static final String SYMBOL_SOURCE = "SYMBOL_SOURCE";
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  private static final String INHERITANCE = "InheritanceModesGene";
  private VCFHeader vcfHeader = null;
  private FieldMetadataService fieldMetadataService = null;
  @Getter
  private String vepFieldId = null;
  @Getter
  private int geneIndex = -1;
  @Getter
  private int geneSourceIndex = -1;
  @Getter
  private int inheritanceIndex = -1;
  @Getter
  private int alleleNumIndex = -1;
  @Getter
  private int classIndex = -1;

  public VepMetadata(VCFHeader vcfHeader, FieldMetadataService fieldMetadataService) {
      this.vcfHeader = vcfHeader;
      this.fieldMetadataService = fieldMetadataService;

      init();
  }

  private static boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
  }

  private void init() {
    for (VCFInfoHeaderLine vcfInfoHeaderLine : vcfHeader.getInfoHeaderLines()) {
      if (canMap(vcfInfoHeaderLine)) {
        this.vepFieldId = vcfInfoHeaderLine.getID();
        FieldMetadata fieldMetadata = fieldMetadataService.load(
            vcfInfoHeaderLine);
        Map<String, NestedField> nestedFields = fieldMetadata.getNestedFields();
        geneIndex = nestedFields.get(GENE) != null ? nestedFields.get(GENE).getIndex():-1;
        geneSourceIndex = nestedFields.get(SYMBOL_SOURCE) != null ? nestedFields.get(SYMBOL_SOURCE).getIndex():-1;
        inheritanceIndex = nestedFields.get(INHERITANCE) != null ? nestedFields.get(INHERITANCE).getIndex():-1;
        alleleNumIndex = nestedFields.get(ALLELE_NUM) != null ? nestedFields.get(ALLELE_NUM).getIndex():-1;
        classIndex = nestedFields.get(VIP_CLASS) != null ? nestedFields.get(VIP_CLASS).getIndex():-1;
        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  //For testing purposes
  public VepMetadata(String vepFieldId, int geneIndex, int geneSourceIndex, int inheritanceIndex, int alleleNumIndex, int classIndex) {
    this.vepFieldId = vepFieldId;
    this.geneIndex = geneIndex;
    this.geneSourceIndex = geneSourceIndex;
    this.inheritanceIndex = inheritanceIndex;
    this.alleleNumIndex = alleleNumIndex;
    this.classIndex = classIndex;
  }
}
