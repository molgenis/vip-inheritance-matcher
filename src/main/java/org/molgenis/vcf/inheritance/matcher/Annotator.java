package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.TRUE;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfFilterHeaderLines;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfFormatHeaderLines;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfInfoHeaderLines;

import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.utils.UnexpectedEnumException;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class Annotator {

  public static final String INHERITANCE_MODES = "VI";
  public static final String SUBINHERITANCE_MODES = "VIS";
  public static final String POSSIBLE_COMPOUND = "VIC";
  public static final String DENOVO = "VID";
  public static final String INHERITANCE_MATCH = "VIM";
  public static final String MATCHING_GENES = "VIG";

  VCFHeader annotateHeader(VCFHeader vcfHeader) {
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
        "Possible Compound hetrozygote variants."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Denovo status."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Match: Genotypes, affected statuses and known gene inheritance patterns match."));
    vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "Genes with an inheritance match."));

    Set<VCFHeaderLine> headerLines = new LinkedHashSet<>();
    //workaround for "Escaped doublequotes in INFO descriptions result in invalid VCF file"
    // https://github.com/samtools/htsjdk/issues/1661
    headerLines.addAll(fixVcfInfoHeaderLines(
        vcfHeader));
    headerLines.addAll(fixVcfFormatHeaderLines(
        vcfHeader));
    headerLines.addAll(fixVcfFilterHeaderLines(vcfHeader));
    headerLines.addAll(vcfHeader.getOtherHeaderLines());
    headerLines.addAll(vcfHeader.getContigLines());

    vcfHeader = new VCFHeader(headerLines, vcfHeader.getGenotypeSamples());
    return vcfHeader;
  }

  VariantContext annotateInheritance(VariantContext vc, Map<String, Pedigree> familyMap,
      Map<String, Annotation> annotationMap) {
    GenotypesContext genotypesContext = GenotypesContext.copy(vc.getGenotypes());
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    for (Entry<String, Pedigree> sampleFamilyEntry : familyMap.entrySet()) {
      for (Sample sample : sampleFamilyEntry.getValue().getMembers().values()) {
        String sampleId = sample.getPerson().getIndividualId();
        if (annotationMap.containsKey(sampleId)) {
          annotateGenotype(vc, annotationMap.get(sampleId), genotypesContext, sample);
        }
      }
    }
    return variantContextBuilder.genotypes(genotypesContext).make();
  }

  private void annotateGenotype(VariantContext vc, Annotation annotation,
      GenotypesContext genotypesContext, Sample sample) {
    if (vc.getGenotype(sample.getPerson().getIndividualId()) != null) {
      GenotypeBuilder genotypeBuilder = new GenotypeBuilder(
          vc.getGenotype(sample.getPerson().getIndividualId()));
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
      String compounds = annotation.getInheritance().getCompounds().isEmpty() ? null : String
          .join(",", annotation.getInheritance().getCompounds());
      genotypeBuilder.attribute(POSSIBLE_COMPOUND, compounds);
      genotypeBuilder.attribute(DENOVO, annotation.getInheritance().isDenovo() ? "1" : "0");
      InheritanceMatch match = annotation.getInheritance().getMatch();
      String inheritanceMatch = mapInheritanceMatch(match);
      genotypeBuilder
              .attribute(INHERITANCE_MATCH, inheritanceMatch);
      if (match == TRUE) {
        genotypeBuilder
            .attribute(MATCHING_GENES, annotation.getMatchingGenes().stream().sorted().collect(
                Collectors.joining(",")));
      }

      genotypesContext.replace(genotypeBuilder.make());
    }
  }

  private static String mapInheritanceMatch(InheritanceMatch match) {
    String inheritanceMatch;
    switch (match){
      case TRUE -> inheritanceMatch = "1";
      case FALSE -> inheritanceMatch = "0";
      case UNKNOWN -> inheritanceMatch = null;
      default -> throw new UnexpectedEnumException(match);
    }
    return inheritanceMatch;
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
