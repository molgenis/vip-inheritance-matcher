package org.molgenis.vcf.inheritance.matcher.vcf.meta;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Map;

import lombok.Getter;
import org.molgenis.vcf.utils.metadata.FieldIdentifier;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.NestedAttributes;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;

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
        FieldMetadatas fieldMetadatas = fieldMetadataService.load(vcfHeader, Map.of(FieldIdentifier.builder()
                .type(org.molgenis.vcf.utils.metadata.FieldType.INFO).name(vepFieldId).build(), NestedAttributes.builder().prefix(INFO_DESCRIPTION_PREFIX).seperator("|").build()));
        FieldMetadata vepField = fieldMetadatas.getInfo().get(vepFieldId);
        if (vepField == null) {
          throw new MissingInfoException(vepFieldId);
        }
        Map<String, NestedFieldMetadata> nestedFieldsMeta = vepField.getNestedFields();
        if (nestedFieldsMeta == null) {
          throw new MissingVepMetadataException(vepFieldId);
        }
        geneIndex = getIndex(nestedFieldsMeta, GENE);
        geneSourceIndex = getIndex(nestedFieldsMeta, SYMBOL_SOURCE);
        inheritanceIndex = getIndex(nestedFieldsMeta, INHERITANCE);
        alleleNumIndex = getIndex(nestedFieldsMeta, ALLELE_NUM);
        classIndex = getIndex(nestedFieldsMeta, VIP_CLASS);
        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  private static int getIndex(Map<String, NestedFieldMetadata> nestedFields, String fieldName) {
    return nestedFields.get(fieldName) != null ? nestedFields.get(fieldName).getIndex() : -1;
  }
}
