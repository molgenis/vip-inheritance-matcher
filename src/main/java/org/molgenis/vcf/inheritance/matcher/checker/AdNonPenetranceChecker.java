package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class AdNonPenetranceChecker extends InheritanceChecker{

  public MatchEnum check(
          VcfRecord vcfRecord, Pedigree family, MatchEnum isAd) {
    if (!VariantContextUtils.onAutosome(vcfRecord) || isAd == TRUE) {
      return FALSE;
    }

    return checkFamily(vcfRecord, family);
  }

  MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
    EffectiveGenotype sampleGt = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED -> {
        if (sampleGt.isMixed()) {
          return sampleGt.hasAltAllele() ? TRUE : POTENTIAL;
        }
        if(sampleGt.isNoCall()){
          return POTENTIAL;
        }else{
          return sampleGt.isHomRef() ? FALSE : TRUE;
        }
      }
      case UNAFFECTED -> {
        return TRUE;
      }
      case MISSING -> {
        return POTENTIAL;
      }
      default -> throw new IllegalArgumentException();
    }
  }
}

