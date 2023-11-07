package org.molgenis.vcf.inheritance.matcher;

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
import org.molgenis.vcf.inheritance.matcher.model.VariantContextGenes;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.model.FieldMetadata;
import org.molgenis.vcf.utils.model.NestedField;

public class VepMapper {

  public static final String GENE = "Gene";
  public static final String SYMBOL_SOURCE = "SYMBOL_SOURCE";
  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  private static final String INHERITANCE = "InheritanceModesGene";
  public static final String INCOMPLETE_PENETRANCE = "IncompletePenetrance";
  private String vepFieldId = null;
  private final FieldMetadataService fieldMetadataService;
  private int geneIndex = -1;
  private int geneSourceIndex = -1;
  private int inheritanceIndex = -1;
  private int incompletePenetranceIndex = -1;

  public VepMapper(VCFFileReader vcfFileReader, FieldMetadataService fieldMetadataService) {
    this.fieldMetadataService = fieldMetadataService;
    init(vcfFileReader);
  }

  private static boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
  }

  private void init(VCFFileReader vcfFileReader) {
    VCFHeader vcfHeader = vcfFileReader.getFileHeader();
    for (VCFInfoHeaderLine vcfInfoHeaderLine : vcfHeader.getInfoHeaderLines()) {
      if (canMap(vcfInfoHeaderLine)) {
        this.vepFieldId = vcfInfoHeaderLine.getID();
        FieldMetadata fieldMetadata = fieldMetadataService.load(
            vcfInfoHeaderLine);
        Map<String, NestedField> nestedFields = fieldMetadata.getNestedFields();
        geneIndex = nestedFields.get(GENE) != null ? nestedFields.get(GENE).getIndex():-1;
        geneSourceIndex = nestedFields.get(SYMBOL_SOURCE) != null ? nestedFields.get(SYMBOL_SOURCE).getIndex():-1;
        inheritanceIndex = nestedFields.get(INHERITANCE) != null ? nestedFields.get(INHERITANCE).getIndex():-1;
        incompletePenetranceIndex = nestedFields.get(INCOMPLETE_PENETRANCE) != null ? nestedFields.get(INCOMPLETE_PENETRANCE).getIndex():-1;

        return;
      }
    }
    throw new MissingInfoException("VEP");
  }

  public VariantContextGenes getGenes(VariantContext vc) {
    return getGenes(vc, emptyMap());
  }

  public VariantContextGenes getGenes(VariantContext vc, Map<String, Gene> knownGenes) {
    VariantContextGenes.VariantContextGenesBuilder genesBuilder = VariantContextGenes.builder();
    Map<String, Gene> genes = new HashMap<>();
    List<String> vepValues = vc.getAttributeAsStringList(vepFieldId, "");
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
        boolean isIncompletePenetrance = false;
        if (incompletePenetranceIndex != -1) {
          isIncompletePenetrance = vepSplit[incompletePenetranceIndex].equals("1");
        }
        genes.put(gene, new Gene(gene, source, isIncompletePenetrance, modes));
      } else {
        genes.put(gene, knownGenes.get(gene));
      }
    }
    genesBuilder.genes(genes);
    return genesBuilder.build();
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
    Map<String, Gene> genes = getGenes(variantContext).getGenes();
    return genes.values().stream().anyMatch(Gene::isIncompletePenetrance);
  }
}
