package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import java.util.Optional;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

/**
 * Autosomal dominant (AD) inheritance pattern matcher
 */
public class AdChecker {

  private AdChecker() {
  }

  /**
   * Check whether the AD inheritance pattern could match for a variant in a pedigree
   */
  public static boolean check(
      VariantContext variantContext, Map<String, Sample> family) {
    if (!VariantContextUtils.onAutosome(variantContext)) {
      return false;
    }

    for (Sample sample : family.values()) {
      Optional<Genotype> genotype = VariantContextUtils.getGenotype(variantContext, sample);
      if (genotype.isPresent() && !check(genotype.get(), sample)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether the AD inheritance pattern could match for a variant as seen in an individual
   */
  private static boolean check(Genotype genotype, Sample sample) {
    if (!genotype.isCalled() || genotype.isMixed()) {
      return true;
    }

    switch (sample.getAffectedStatus()) {
      case AFFECTED:
        return genotype.isHet();
      case UNAFFECTED:
        return genotype.isHomRef();
      case UNRECOGNIZED:
      case MISSING:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
