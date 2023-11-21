package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class AdNonPenetranceChecker {
  private AdNonPenetranceChecker(){}

  public static Boolean check(
          VariantContext variantContext, Pedigree family) {
    if (!VariantContextUtils.onAutosome(variantContext) || AdChecker.check(variantContext, family) == Boolean.TRUE) {
      return false;
    }

    for (Sample sample : family.getMembers().values()) {
      Boolean result = checkSample(sample, variantContext);
      if(result == null){
        return null;
      } else if(result == false){
        return false;
      }
    }
    return true;
  }

  private static Boolean checkSample(Sample sample, VariantContext variantContext) {
    Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        if(sampleGt.isMixed()){
          return hasVariant(sampleGt) ? true : null;
        }
        return sampleGt.isNoCall() ? null : !sampleGt.isHomRef();
      case UNAFFECTED:
        return true;
      case MISSING:
        return null;
      default:
        throw new IllegalArgumentException();
    }
  }
}

