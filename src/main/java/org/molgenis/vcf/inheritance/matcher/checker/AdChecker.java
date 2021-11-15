package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class AdChecker {

  private AdChecker() {
  }

  public static boolean check(
      VariantContext variantContext, Map<String, Sample> family) {

    if (!(variantContext.getContig().equals("X") || variantContext.getContig().startsWith("chrX"))) {
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

  private static boolean checkSample(VariantContext variantContext,
      Sample currentSample, Genotype genotype) {
    if (genotype != null && genotype.isCalled()) {
      boolean affected = currentSample.getAffectedStatus() == AffectedStatus.AFFECTED;
      if (affected) {
        return genotype.getAlleles().stream()
            .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele)) && genotype
            .isHet();
      } else {
        return genotype.isHomRef();
      }
    }
    return true;
  }
}
