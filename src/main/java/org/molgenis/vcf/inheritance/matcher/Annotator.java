package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class Annotator {

  public static final String INHERITANCE_MODES = "VI";
  public static final String SUBINHERITANCE_MODES = "VIS";
  public static final String POSSIBLE_COMPOUND = "VIC";
  public static final String DENOVO = "VID";
  public static final String INHERITANCE_MATCH = "VIM";
  public static final String MATCHING_GENES = "VIG";
  public static final String NONE_PENETRANCE_GENES = "VIPN";

  VCFHeader annotateHeader(VCFHeader vcfHeader, boolean isAnnotateNonPenetrance) {
    vcfHeader
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "An enumeration of possible inheritance modes."));
    vcfHeader
        .addMetaDataLine(new VCFFormatHeaderLine(SUBINHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "An enumeration of possible sub inheritance modes like e.g. compound, non penetrance."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
        VCFHeaderLineType.String,
        "Inheritance Compound status."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Denovo status."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Match status."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "Genes with an inheritance match."));
    if(isAnnotateNonPenetrance) {
      vcfHeader.addMetaDataLine(
          new VCFInfoHeaderLine(NONE_PENETRANCE_GENES, VCFHeaderLineCount.UNBOUNDED,
              VCFHeaderLineType.String,
              "Genes that were treated as non penetrant for the inheritance matching."));
    }
    return vcfHeader;
  }

  VariantContext annotateInheritance(VariantContext vc, Map<String, Map<String, Sample>> familyMap,
      Map<String, Annotation> annotationMap) {
    GenotypesContext genotypesContext = GenotypesContext.copy(vc.getGenotypes());
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    for (Entry<String, Map<String, Sample>> sampleFamilyEntry : familyMap.entrySet()) {
      for (Sample sample : sampleFamilyEntry.getValue().values()) {
        String sampleId = sample.getIndividualId();
        if (annotationMap.containsKey(sampleId)) {
          annotateGenotype(vc, annotationMap.get(sampleId), genotypesContext, sample);
        }
      }
    }
    return variantContextBuilder.genotypes(genotypesContext).make();
  }

  VariantContext annotateNonPenetranceGenes(VariantContext vc, Set<String> nonPenetranceGenes) {
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    variantContextBuilder.attribute(NONE_PENETRANCE_GENES,nonPenetranceGenes);
    return variantContextBuilder.make();
  }

  private void annotateGenotype(VariantContext vc, Annotation annotation,
      GenotypesContext genotypesContext, Sample sample) {
    if (vc.getGenotype(sample.getIndividualId()) != null) {
      GenotypeBuilder genotypeBuilder = new GenotypeBuilder(
          vc.getGenotype(sample.getIndividualId()));
      String inheritanceModes = String
          .join(",", mapInheritanceModes(annotation.getInheritance()));
      if (!inheritanceModes.isEmpty()) {
        genotypeBuilder.attribute(INHERITANCE_MODES, inheritanceModes);
      }
      String subinheritanceModes = String
          .join(",", mapSubinheritanceModes(annotation.getInheritance()));
      if (!subinheritanceModes.isEmpty()) {
        genotypeBuilder.attribute(SUBINHERITANCE_MODES, subinheritanceModes);
      }
      String compounds = annotation.getInheritance().getCompounds().isEmpty()?null:String
          .join(",", annotation.getInheritance().getCompounds());
      genotypeBuilder.attribute(POSSIBLE_COMPOUND, compounds);
      genotypeBuilder.attribute(DENOVO, annotation.getInheritance().isDenovo() ? "1" : "0");
      Set<String> genes = annotation.getMatchingGenes();
      boolean isMatch = !(genes == null || genes.isEmpty());
      genotypeBuilder
          .attribute(INHERITANCE_MATCH,
              isMatch
                  ? "1" : "0");
      if (isMatch) {
        genotypeBuilder.attribute(MATCHING_GENES, annotation.getMatchingGenes().stream().sorted().collect(
            Collectors.joining(",")));
      }

      genotypesContext.replace(genotypeBuilder.make());
    }
  }

  private Set<String> mapSubinheritanceModes(Inheritance inheritance) {
    Set<String> result = new HashSet<>();
    for (SubInheritanceMode inheritanceModeEnum : inheritance.getSubInheritanceModes()) {
      result.add(inheritanceModeEnum.name());
    }
    return result;
  }

  private Set<String> mapInheritanceModes(Inheritance inheritance) {
    Set<String> result = new HashSet<>();
    for (InheritanceMode inheritanceMode : inheritance.getInheritanceModes()) {
      result.add(inheritanceMode.name());
    }
    return result;
  }

}
