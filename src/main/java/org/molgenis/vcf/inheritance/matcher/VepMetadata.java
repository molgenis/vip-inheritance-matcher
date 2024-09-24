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
  private final VCFHeader vcfHeader;
  private final FieldMetadataService fieldMetadataService;
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
        geneIndex = getIndex(nestedFields, GENE);
        geneSourceIndex = getIndex(nestedFields, SYMBOL_SOURCE);
        inheritanceIndex = getIndex(nestedFields, INHERITANCE);
        alleleNumIndex = getIndex(nestedFields, ALLELE_NUM);
        classIndex = getIndex(nestedFields, VIP_CLASS);
        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  private static int getIndex(Map<String, NestedField> nestedFields, String fieldName) {
    return nestedFields.get(fieldName) != null ? nestedFields.get(fieldName).getIndex() : -1;
  }
}
