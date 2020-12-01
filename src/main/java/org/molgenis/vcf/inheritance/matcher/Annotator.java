package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;
import org.springframework.stereotype.Component;

@Component
public class Annotator {

  public static final String INHERITANCE_MODES = "MDS";
  public static final String POSSIBLE_COMPOUND = "CMP";
  public static final String DENOVO = "DNV";
  public static final String INHERITANCE_MATCH = "MATCH";
  public static final String MATCHING_GENES = "GENES";

  VCFHeader annotateHeader(VCFHeader vcfHeader) {
    vcfHeader
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "Predicted inheritance modes."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
        VCFHeaderLineType.Integer,
        "Possible compound status for AR inheritance modes, 1 = true, 0 = false."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
        VCFHeaderLineType.Integer,
        "Denovo status, 1 = true, 0 = false."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
        VCFHeaderLineType.String,
        "Does inheritance match for sample and genes, 1 = true, 0 = false."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "Genes for which inheritance modes of the sample and gene match."));
    return vcfHeader;
  }

  VariantContext annotate(VariantContext vc, Map<String, Annotation> annotations,
      Map<String, String> samples) {
    GenotypesContext genotypesContext = GenotypesContext.copy(vc.getGenotypes());
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    for (Entry<String, String> sampleFamilyEntry : samples.entrySet()) {
      String familyId = sampleFamilyEntry.getValue();
      String sampleId = sampleFamilyEntry.getKey();
      if (annotations.containsKey(familyId)) {
        annotateGenotype(vc, annotations, genotypesContext, familyId, sampleId);
      }
    }
    return variantContextBuilder.genotypes(genotypesContext).make();
  }

  private void annotateGenotype(VariantContext vc, Map<String, Annotation> annotations,
      GenotypesContext genotypesContext, String familyId, String sampleId) {
    Annotation annotation = annotations.get(familyId);
    GenotypeBuilder genotypeBuilder = new GenotypeBuilder(vc.getGenotype(sampleId));
    String inheritanceModes = String
        .join(",", mapInheritanceModes(annotation.getInheritanceModes()));
    if (!inheritanceModes.isEmpty()) {
      genotypeBuilder.attribute(INHERITANCE_MODES, inheritanceModes);
    }
    String isCompound = isCompound(annotation.getInheritanceModes());
    if (isCompound != null) {
      genotypeBuilder.attribute(POSSIBLE_COMPOUND, isCompound);
    }
    genotypeBuilder.attribute(DENOVO, annotation.isDenovo() ? "1" : "0");
    List<String> genes = annotation.getMatchingGenes();
    boolean isMatch = !(genes == null || genes.isEmpty());
    genotypeBuilder
        .attribute(INHERITANCE_MATCH,
            isMatch
                ? "1" : "0");
    if (isMatch) {
      genotypeBuilder.attribute(MATCHING_GENES, annotation.getMatchingGenes());
    }

    genotypesContext.replace(genotypeBuilder.make());
  }

  private Set<String> mapInheritanceModes(Set<InheritanceMode> inheritanceModes) {
    Set<String> result = new HashSet<>();
    for (InheritanceMode inheritanceMode : inheritanceModes) {
      result.add(inheritanceMode.getInheritanceModeEnum().name());
    }
    return result;
  }

  private String isCompound(Set<InheritanceMode> inheritanceModes) {
    Boolean isCompound = null;
    for (InheritanceMode inheritanceMode : inheritanceModes) {
      if (inheritanceMode.getInheritanceModeEnum() == InheritanceModeEnum.AR && !Boolean.TRUE
          .equals(isCompound)) {
        isCompound = inheritanceMode.getSubInheritanceMode() == SubInheritanceMode.COMP;
      }
    }
    return mapCompound(isCompound);
  }

  private String mapCompound(Boolean compound) {
    if (Boolean.TRUE.equals(compound)) {
      return "1";
    } else {
      if (compound == null) {
        return null;
      } else {
        return "0";
      }
    }
  }
}
