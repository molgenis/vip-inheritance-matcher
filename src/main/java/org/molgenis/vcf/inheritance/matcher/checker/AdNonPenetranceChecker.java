package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class AdNonPenetranceChecker extends InheritanceChecker{

  public MatchEnum check(
          VariantContext variantContext, Pedigree family, MatchEnum isAd) {
    if (!VariantContextUtils.onAutosome(variantContext) || isAd == TRUE) {
      return FALSE;
    }

    return checkFamily(variantContext, family);
  }

  MatchEnum checkSample(Sample sample, VariantContext variantContext) {
    Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> {
        if (sampleGt.isMixed()) {
          return hasVariant(sampleGt) ? TRUE : POTENTIAL;
        }
        if(sampleGt.isNoCall()){
          return POTENTIAL;
        }else{
          return sampleGt.isHomRef() ? FALSE : TRUE;
        }
      }
      case UNAFFECTED -> {
        return TRUE;
      }
      case MISSING -> {
        return POTENTIAL;
      }
      default -> throw new IllegalArgumentException();
    }
  }
}

