package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

public class ArChecker {

  public boolean check(
      VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext)) {
      for (Individual currentIndividual : family.getMembers().values()) {
        Genotype genotype = variantContext.getGenotype(currentIndividual.getId());
        if (genotype != null && !checkSample(variantContext, currentIndividual, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkSample(VariantContext variantContext,
      Individual currentIndividual, Genotype genotype) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentIndividual.getAffectedStatus() == AffectedStatus.AFFECTED;
      if (affected) {
        return genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
            .isHom();
      } else {
        //Alt present, only allowed if it is hetrozygous
        if (genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele))) {
          return genotype.isHet();
        }
      }
    }
    return true;
  }
}
