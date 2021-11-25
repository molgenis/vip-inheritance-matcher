package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class XlrChecker extends XlChecker {

  protected boolean checkSample(VariantContext variantContext,
      Individual currentIndividual, Genotype genotype) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentIndividual.getAffectedStatus() == AffectedStatus.AFFECTED;
      Sex sex = currentIndividual.getSex();
      if (getSex(sex, genotype) == MALE) {
        if (affected) {
          // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
          return genotype.getAlleles().stream()
              .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
        } else {
          // Healthy males cannot carry the variant
          return genotype.getAlleles().stream()
              .noneMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
        }
      } else if (getSex(sex, genotype) == Sex.FEMALE) {
        if (affected) {
          // Affected females have to be hom. alt.
          return genotype.getAlleles().stream()
              .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
              .isHom();
        } else {
          // Healthy females cannot be hom. alt.
          return !(genotype.getAlleles().stream()
              .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
              .isHom());
        }
      }
    }
    return true;
  }
}
