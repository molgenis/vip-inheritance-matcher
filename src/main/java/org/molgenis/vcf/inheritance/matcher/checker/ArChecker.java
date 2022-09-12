package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArChecker {

  public boolean check(
      VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext)) {
      for (Sample currentSample : family.getMembers().values()) {
        Genotype genotype = variantContext.getGenotype(currentSample.getPerson().getIndividualId());
        if (genotype != null && !checkSample(currentSample, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkSample(Sample sample, Genotype genotype) {
    if (genotype == null || !genotype.isCalled()) {
      return true;
    }

    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        return genotype.getAlleles().stream()
            .allMatch(allele -> allele.isNonReference() || allele.isNoCall());
      case UNAFFECTED:
        //Alt present, only allowed if it is hetrozygous or the other allele is missing
        return genotype.isHomRef() || genotype.isHet() || genotype.isMixed();
      case MISSING:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }
}
