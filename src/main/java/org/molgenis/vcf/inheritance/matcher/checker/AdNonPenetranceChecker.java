package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

public class AdNonPenetranceChecker {

  private final VepMapper vepMapper;

  public AdNonPenetranceChecker(VepMapper vepMapper) {
    this.vepMapper = vepMapper;
  }

  public boolean check(VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext) && vepMapper.containsIncompletePenetrance(variantContext)
        && !AdChecker.check(variantContext, family)) {
      for (Individual currentIndividual : family.getMembers().values()) {
        Genotype genotype = variantContext.getGenotype(currentIndividual.getId());
        if (!checkSample(variantContext, currentIndividual, genotype)) {
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
            .isHet();
      } else {
        return genotype.isHomRef() || genotype.isHet();
      }
    }
    return true;
  }
}
