package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.utils.sample.model.AffectedStatus.MISSING;
import static org.molgenis.vcf.utils.sample.model.AffectedStatus.UNAFFECTED;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class AdNonPenetranceChecker {
  private AdNonPenetranceChecker(){}

  public static boolean check(VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext) && !AdChecker.check(variantContext, family)) {
      for (Sample currentSample : family.getMembers().values()) {
        Genotype genotype = variantContext.getGenotype(currentSample.getPerson().getIndividualId());
        if (!checkSample(currentSample, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static boolean checkSample(Sample sample, Genotype genotype) {

    if (genotype == null || !genotype.isCalled() || !genotype.isHomRef() || genotype.isMixed()) {
      //Due to the incomplete penetrance individuals can have the variant independent of their affected status
      return true;
    }

    //HOMREF individuals cannot be affected
    return genotype.isHomRef() && (sample.getPerson().getAffectedStatus() == UNAFFECTED
        || sample.getPerson().getAffectedStatus() == MISSING);
  }
}
