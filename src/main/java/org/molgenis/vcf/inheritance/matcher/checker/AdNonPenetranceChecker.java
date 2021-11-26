package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.AffectedStatus.AFFECTED;
import static org.molgenis.vcf.inheritance.matcher.model.AffectedStatus.UNAFFECTED;
import static org.molgenis.vcf.inheritance.matcher.model.AffectedStatus.UNKNOWN;

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
        if (!checkSample(currentIndividual, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkSample(Individual individual, Genotype genotype) {

    if (genotype == null || !genotype.isCalled()) {
      throw new IllegalArgumentException("missing");
    }else if(genotype.isMixed()){
      throw new IllegalArgumentException("mixed");
    }

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        return genotype.isHet();
      case UNAFFECTED:
      case UNKNOWN:
        return genotype.isHomRef() || genotype.isHet();
      default:
        throw new IllegalArgumentException();
    }
  }
}
