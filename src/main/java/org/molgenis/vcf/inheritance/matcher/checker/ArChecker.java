package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class ArChecker extends InheritanceChecker{

  public MatchEnum check(
          VcfRecord vcfRecord, Pedigree family) {
    if (!onAutosome(vcfRecord)) {
      return FALSE;
    }

    return checkFamily(vcfRecord, family);
  }

  @Override
  MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
    EffectiveGenotype sampleGt = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
    if (sampleGt == null || sampleGt.isNoCall()) {
      return POTENTIAL;
    } else {
      if (sampleGt.isMixed()) {
        return checkMixed(sample, sampleGt);
      } else {
        if (sampleGt.hasAltAllele()) {
          return checkSampleWithVariant(sample, sampleGt);
        } else {
          return checkSampleWithoutVariant(sample);
        }
      }
    }
  }

  private static MatchEnum checkSampleWithoutVariant(Sample sample) {
    return switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> FALSE;
      case UNAFFECTED -> TRUE;
      case MISSING -> POTENTIAL;
    };
  }

  private static MatchEnum checkSampleWithVariant(Sample sample, EffectiveGenotype sampleGt) {
    return switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> sampleGt.isHom() ? TRUE : FALSE;
      case UNAFFECTED -> sampleGt.isHet() ? TRUE : FALSE;
      case MISSING -> POTENTIAL;
    };
  }

  private static MatchEnum checkMixed(Sample sample, EffectiveGenotype sampleGt) {
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> {
        if (!sampleGt.hasAltAllele()) {
          return FALSE;
        } else {
          return POTENTIAL;
        }
      }
      case UNAFFECTED, MISSING -> {
        return POTENTIAL;
      }
      default -> throw new IllegalArgumentException();
    }
  }
}
