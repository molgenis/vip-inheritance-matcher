package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import org.molgenis.vcf.utils.sample.model.Sample;

public class XldChecker extends XlChecker {

  protected boolean checkIndividual(Sample sample, Genotype genotype) {
    if (genotype == null || !genotype.isCalled()) {
      return true;
    }

    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        // Affected individuals have to be het. or hom. alt.
        return genotype.getAlleles().stream()
            .anyMatch(allele -> allele.isNonReference() || allele.isNoCall());
      case UNAFFECTED:
        switch (getSex(sample.getPerson().getSex(), genotype)) {
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
      case MISSING:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
