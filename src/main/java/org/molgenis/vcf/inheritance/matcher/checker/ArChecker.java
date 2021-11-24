package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.ChromosomeUtils;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Chromosome;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class ArChecker {

  public boolean check(
      VariantContext variantContext, Map<String, Sample> family) {
    if ((ChromosomeUtils.mapChromosomeId(variantContext.getContig()) != Chromosome.X)) {
      for (Sample currentSample : family.values()) {
        Genotype genotype = variantContext.getGenotype(currentSample.getIndividualId());
        if (genotype != null && !checkSample(variantContext, currentSample, genotype)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkSample(VariantContext variantContext,
      Sample currentSample, Genotype genotype) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentSample.getAffectedStatus() == AffectedStatus.AFFECTED;
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
