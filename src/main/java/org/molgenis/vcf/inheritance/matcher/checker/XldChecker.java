package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class XldChecker extends XlChecker {

  protected boolean checkSample(VariantContext variantContext,
      Individual currentIndividual, Genotype genotype) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentIndividual.getAffectedStatus() == AffectedStatus.AFFECTED;
      Sex sex = currentIndividual.getSex();
      if (affected) {
        // Affected individuals have to be het. or hom. alt.
        return genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) || genotype.isMixed();
      } else if (getSex(sex, genotype) == Sex.MALE) {
        // Healthy males cannot carry the variant
        return genotype.getAlleles().stream()
            .noneMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
      } else if (getSex(sex, genotype) == Sex.FEMALE) {
        // Healthy females can carry the variant (because of X inactivation)
        return !(genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
            .isHom());
      }
    }
    return true;
  }
}
