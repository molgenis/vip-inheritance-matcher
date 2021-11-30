package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import org.molgenis.vcf.inheritance.matcher.model.Individual;

public class XlrChecker extends XlChecker {

  protected boolean checkIndividual(Individual individual, Genotype genotype) {
    if (genotype == null || !genotype.isCalled()) {
      return true;
    }

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        switch (getSex(individual.getSex(), genotype)) {
          case MALE:
            // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
            return genotype.getAlleles().stream()
                .anyMatch(allele -> allele.isNonReference() || allele.isNoCall());
          case FEMALE:
            // Affected females have to be hom. alt.
            return (genotype.getAlleles().stream()
                .allMatch(allele -> allele.isNonReference() || allele.isNoCall()));
          default:
            throw new IllegalArgumentException();
        }
      case UNAFFECTED:
        switch (getSex(individual.getSex(), genotype)) {
          case MALE:
            // Healthy males cannot carry the variant
            return genotype.getAlleles().stream()
                .noneMatch(Allele::isNonReference);
          case FEMALE:
            // Healthy females cannot be hom. alt.
            return !(genotype.getAlleles().stream()
                .anyMatch(allele -> allele.isNonReference() || allele.isNoCall())
                && genotype
                .isHom());
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
