package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.Individual;

public class XldChecker extends XlChecker {

  protected boolean checkIndividual(VariantContext variantContext,
      Individual individual, Genotype genotype) {
    if (genotype == null || !genotype.isCalled()) {
      return true;
    }

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        // Affected individuals have to be het. or hom. alt.
        return genotype.getAlleles().stream()
            .anyMatch(allele -> allele.isNonReference() || allele.isNoCall());
      case UNAFFECTED:
        switch (getSex(individual.getSex(), genotype)) {
          case MALE:
            // Healthy males cannot carry the variant
            return genotype.getAlleles().stream()
                .allMatch(allele -> allele.isReference() || allele.isNoCall());
          case FEMALE:
            // Healthy females can carry the variant (because of X inactivation)
            return genotype.isHet() || genotype.isMixed() || genotype.isHomRef();
          default:
            throw new IllegalArgumentException();
        }
      case UNKNOWN:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
