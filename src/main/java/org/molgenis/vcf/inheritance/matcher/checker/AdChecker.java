package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Optional;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

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

    for (Individual individual : family.getMembers().values()) {
      Optional<Genotype> genotype = VariantContextUtils.getGenotype(variantContext, individual);
      if (genotype.isPresent() && !check(genotype.get(), individual, variantContext)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether the AD inheritance pattern could match for a variant as seen in an individual
   */
  private static boolean check(Genotype genotype, Individual individual,
      VariantContext variantContext) {
    if (!genotype.isCalled()) {
      return true;
    }

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        return genotype.isHet() || genotype.isMixed();
      case UNAFFECTED:
        return genotype.isHomRef() || (genotype.getAlleles().stream()
            .noneMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
            .isMixed());
      case UNKNOWN:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
