package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptyMap;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.VariantContextGenes;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.model.FieldMetadata;
import org.molgenis.vcf.utils.model.NestedField;

public class VepMapper {

  public static final String GENE = "Gene";
  public static final String ALLELE_NUM = "ALLELE_NUM";
  public static final String VIP_CLASS = "VIPC";
  public static final String SYMBOL_SOURCE = "SYMBOL_SOURCE";
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  private static final String INHERITANCE = "InheritanceModesGene";
  private String vepFieldId = null;
    private final VCFHeader vcfHeader;
    private final FieldMetadataService fieldMetadataService;
  private int geneIndex = -1;
  private int geneSourceIndex = -1;
  private int inheritanceIndex = -1;
  private int alleleNumIndex = -1;
  private int classIndex = -1;

  public VepMapper(VCFHeader vcfHeader, FieldMetadataService fieldMetadataService) {
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

  public VariantContextGenes getGenes(VcfRecord record) {
    return getGenes(record, emptyMap());
  }

  public VariantContextGenes getGenes(VcfRecord record, Map<String, Gene> knownGenes) {
    VariantContextGenes.VariantContextGenesBuilder genesBuilder = VariantContextGenes.builder();
    Map<String, Gene> genes = new HashMap<>();
    List<String> vepValues = record.getAttributeAsStringList(vepFieldId);
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      String gene = vepSplit[geneIndex];
      String source = vepSplit[geneSourceIndex];
      if (gene.isEmpty() || source.isEmpty()) {
        genesBuilder.containsVcWithoutGene(true);
        continue;
      }

      if (!knownGenes.containsKey(gene)) {
        Set<InheritanceMode> modes = new HashSet<>();
        if (inheritanceIndex != -1) {
          String[] inheritanceModes
              = vepSplit[inheritanceIndex].split("&");
          mapGeneInheritance(modes, inheritanceModes);
        }
        genes.put(gene, new Gene(gene, source, modes));
      } else {
        genes.put(gene, knownGenes.get(gene));
      }
    }
    genesBuilder.genes(genes);
    return genesBuilder.build();
  }

  public Set<String> getClassesForAllele(VcfRecord record, int alleleIndex){
    List<String> vepValues = record.getAttributeAsStringList(vepFieldId);
    Set<String> classes = new HashSet<>();
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      int csqAlleleIndex = Integer.parseInt(vepSplit[alleleNumIndex]);
      if(csqAlleleIndex == alleleIndex){
        classes.add(vepSplit[classIndex]);
      }
    }
    return classes;
  }

  private void mapGeneInheritance(Set<InheritanceMode> modes, String[] inheritanceModes) {
    for (String mode : inheritanceModes) {
      switch (mode) {
        case "AR" -> modes.add(InheritanceMode.AR);
        case "AD" -> modes.add(InheritanceMode.AD);
        case "XLR" -> modes.add(InheritanceMode.XLR);
        case "XLD" -> modes.add(InheritanceMode.XLD);
        case "XL" -> {
          modes.add(InheritanceMode.XLR);
          modes.add(InheritanceMode.XLD);
        }
        default -> {
          //We ignore all the modes that are not used for matching.
        }
      }
    }
  }
}
