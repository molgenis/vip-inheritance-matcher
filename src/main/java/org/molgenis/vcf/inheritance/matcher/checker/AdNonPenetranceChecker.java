package org.molgenis.vcf.inheritance.matcher.checker;

import static java.util.Collections.emptySet;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class AdNonPenetranceChecker {

  private final VepMapper vepMapper;

  public AdNonPenetranceChecker(VepMapper vepMapper) {
    this.vepMapper = vepMapper;
  }

  public Set<String> check(VariantContext variantContext, Map<String, Sample> family,
      Set<String> nonPenetranceGenes) {
    if (!(variantContext.getContig().equals("X") || variantContext.getContig().equals("chrX"))
        && !AdChecker.check(variantContext, family)) {
      Set<String> nonPenetranceGenesForVariant = vepMapper
          .getNonPenetranceGenesForVariant(variantContext, nonPenetranceGenes);
      for (Sample currentSample : family.values()) {
        Genotype genotype = variantContext.getGenotype(currentSample.getIndividualId());
        if (!checkSample(variantContext, currentSample, genotype,
            !nonPenetranceGenesForVariant.isEmpty())) {
          return emptySet();
        }
      }
      return nonPenetranceGenesForVariant;
    }
    return emptySet();
  }

  private boolean checkSample(VariantContext variantContext,
      Sample currentSample, Genotype genotype, boolean isNonPenetrance) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentSample.getAffectedStatus() == AffectedStatus.AFFECTED;
      if (affected) {
        return genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
            .isHet();
      } else {
        return genotype.isHomRef() || isNonPenetrance;
      }
    }
    return true;
  }
}
