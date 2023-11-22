package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashSet;
import java.util.Set;

public class ArChecker {

  public static Boolean check(
          VariantContext variantContext, Pedigree family) {
    if (!onAutosome(variantContext)) {
      return false;
    }

    return checkFamily(variantContext, family);
  }

  public static Boolean checkFamily(VariantContext variantContext, Pedigree family) {
    Set<Boolean> results = new HashSet<>();
    for (Sample sample : family.getMembers().values()) {
      results.add(checkSample(sample, variantContext));
    }
    if(results.contains(false)){
      return false;
    }else if(results.contains(null)){
      return null;
    }
    return true;
  }

  private static Boolean checkSample(Sample sample, VariantContext variantContext) {
    Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
    if (sampleGt == null || sampleGt.isNoCall()) {
      return null;
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

  private static Boolean checkSampleWithoutVariant(Sample sample) {
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        return false;
      case UNAFFECTED:
        return true;
      case MISSING:
        return null;
      default:
        throw new IllegalArgumentException();
    }
  }

  private static Boolean checkSampleWithVariant(Sample sample, Genotype sampleGt) {
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        return sampleGt.isHom();
      case UNAFFECTED:
        return sampleGt.isHet();
      case MISSING:
        return null;
      default:
        throw new IllegalArgumentException();
    }
  }

  private static Boolean checkMixed(Sample sample, Genotype sampleGt) {
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        if (!hasVariant(sampleGt)) {
          return false;
        }else{
          return null;
        }
      case UNAFFECTED, MISSING:
          return null;
      default:
        throw new IllegalArgumentException();
    }
  }
}
