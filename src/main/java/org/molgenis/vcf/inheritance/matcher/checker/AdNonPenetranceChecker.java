package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.AffectedStatus.UNAFFECTED;
import static org.molgenis.vcf.inheritance.matcher.model.AffectedStatus.UNKNOWN;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
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

    if (genotype == null || !genotype.isCalled() || genotype.isHet() || genotype.isMixed()) {
      //Due to the incomplete penetrance individuals can be HET indepent of their affected status
      return true;
    }

    //HOMREF individuals cannot be affected
    return genotype.isHomRef() && (individual.getAffectedStatus() == UNAFFECTED
        || individual.getAffectedStatus() == UNKNOWN);
  }
}
