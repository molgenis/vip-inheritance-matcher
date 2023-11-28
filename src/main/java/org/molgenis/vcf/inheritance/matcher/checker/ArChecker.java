package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class ArChecker extends InheritanceChecker{

  public MatchEnum check(
          VariantContext variantContext, Pedigree family) {
    if (!onAutosome(variantContext)) {
      return FALSE;
    }

    return checkFamily(variantContext, family);
  }

  @Override
  MatchEnum checkSample(Sample sample, VariantContext variantContext) {
    Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
    if (sampleGt == null || sampleGt.isNoCall()) {
      return POTENTIAL;
    } else {
      if (sampleGt.isMixed()) {
        return checkMixed(sample, sampleGt);
      } else {
        if (hasVariant(sampleGt)) {
          return checkSampleWithVariant(sample, sampleGt);
        } else {
          return checkSampleWithoutVariant(sample);
        }
      }
    }
  }

  private static MatchEnum checkSampleWithoutVariant(Sample sample) {
    return switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> FALSE;
      case UNAFFECTED -> TRUE;
      case MISSING -> POTENTIAL;
    };
  }

  private static MatchEnum checkSampleWithVariant(Sample sample, Genotype sampleGt) {
    return switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> sampleGt.isHom() ? TRUE : FALSE;
      case UNAFFECTED -> sampleGt.isHet() ? TRUE : FALSE;
      case MISSING -> POTENTIAL;
    };
  }

  private static MatchEnum checkMixed(Sample sample, Genotype sampleGt) {
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> {
        if (!hasVariant(sampleGt)) {
          return FALSE;
        } else {
          return POTENTIAL;
        }
      }
      case UNAFFECTED, MISSING -> {
        return POTENTIAL;
      }
      default -> throw new IllegalArgumentException();
    }
  }
}
