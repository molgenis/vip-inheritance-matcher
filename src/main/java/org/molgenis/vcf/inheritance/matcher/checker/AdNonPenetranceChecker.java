package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdNonPenetranceChecker {

    public MatchEnum check(
            VariantRecord variantRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantRecord)) {
            return FALSE;
        }

        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantRecord));
        }
        return CheckerUtils.merge(results);
    }

    MatchEnum checkSample(Sample sample, VariantRecord variantRecord) {
        EffectiveGenotype sampleGt = variantRecord.getGenotype(sample.getPerson().getIndividualId());
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                if (sampleGt != null && sampleGt.isHomRef()) {
                    return FALSE;
                } else if (sampleGt != null && sampleGt.isMixed()) {
                    return sampleGt.hasAltAllele() ? TRUE : POTENTIAL;
                } else {
                    return sampleGt == null ? POTENTIAL : TRUE;
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

