package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashSet;
import java.util.Set;

public class AdNonPenetranceChecker {
  private AdNonPenetranceChecker(){}

  public static MatchEnum check(
          VariantContext variantContext, Pedigree family) {
    if (!VariantContextUtils.onAutosome(variantContext) || AdChecker.check(variantContext, family) == TRUE) {
      return FALSE;
    }

    return checkFamily(variantContext, family);
  }

  public static MatchEnum checkFamily(VariantContext variantContext, Pedigree family) {
    Set<MatchEnum> results = new HashSet<>();
    for (Sample sample : family.getMembers().values()) {
      results.add(checkSample(sample, variantContext));
    }
    if(results.contains(FALSE)){
      return FALSE;
    }else if(results.contains(POTENTIAL)){
      return POTENTIAL;
    }
    return TRUE;
  }

  private static MatchEnum checkSample(Sample sample, VariantContext variantContext) {
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

