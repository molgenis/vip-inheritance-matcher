package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.ChromsomeUtils;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Chromosome;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class AdNonPenetranceChecker {

  private final VepMapper vepMapper;

  public AdNonPenetranceChecker(VepMapper vepMapper) {
    this.vepMapper = vepMapper;
  }

  public boolean check(VariantContext variantContext, Map<String, Sample> family) {
    if ((ChromsomeUtils.mapChromosomeID(variantContext.getContig()) != Chromosome.X) && vepMapper
        .containsIncompletePenetrance(variantContext)
        && !AdChecker.check(variantContext, family)) {
      for (Sample currentSample : family.values()) {
        Genotype genotype = variantContext.getGenotype(currentSample.getIndividualId());
        if (!checkSample(variantContext, currentSample, genotype)) {
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
            .isHet();
      } else {
        return genotype.isHomRef() || genotype.isHet();
      }
    }
    return true;
  }
}
