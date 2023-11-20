package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Optional;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

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
      VariantContext variantContext, Pedigree family) {
    if (!VariantContextUtils.onAutosome(variantContext)) {
      return false;
    }

    for (Sample sample : family.getMembers().values()) {
      Optional<Genotype> genotype = VariantContextUtils.getGenotype(variantContext, sample);
      if (genotype.isPresent() && !check(sample, genotype.get())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether the AD inheritance pattern could match for a variant as seen in an individual
   */
  private static boolean check(Sample sample, Genotype genotype) {
    if (!genotype.isCalled()) {
      return true;
    }

    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        return !genotype.isHomRef() || genotype.isMixed();
      case UNAFFECTED:
        return genotype.getAlleles().stream()
            .allMatch(allele -> allele.isReference() || allele.isNoCall());
      case MISSING:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
