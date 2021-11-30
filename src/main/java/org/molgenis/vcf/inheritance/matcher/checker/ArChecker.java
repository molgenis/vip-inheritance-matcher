package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

public class ArChecker {

  public boolean check(
      VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext)) {
      for (Individual currentIndividual : family.getMembers().values()) {
        Genotype genotype = variantContext.getGenotype(currentIndividual.getId());
        if (genotype != null && !checkSample(currentIndividual, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkSample(Individual individual, Genotype genotype) {
    if (genotype == null || !genotype.isCalled()) {
      return true;
    }

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        return genotype.getAlleles().stream()
            .allMatch(allele -> allele.isNonReference() || allele.isNoCall());
      case UNAFFECTED:
        //Alt present, only allowed if it is hetrozygous or the other allele is missing
          return genotype.isHomRef() || genotype.isHet() || genotype.isMixed();
      case UNKNOWN:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
