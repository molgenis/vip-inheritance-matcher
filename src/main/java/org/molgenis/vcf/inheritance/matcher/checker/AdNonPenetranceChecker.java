package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdNonPenetranceChecker {

    public MatchEnum check(
            VariantGeneRecord variantGeneRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantGeneRecord)) {
            return FALSE;
        }

        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantGeneRecord));
        }
        return CheckerUtils.merge(results);
    }

    MatchEnum checkSample(Sample sample, VariantGeneRecord variantGeneRecord) {
        EffectiveGenotype sampleGt = variantGeneRecord.getGenotype(sample.getPerson().getIndividualId());
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                if (sampleGt.isHomRef()) {
                    return FALSE;
                } else if (sampleGt.isMixed()) {
                    return sampleGt.hasAlt() ? TRUE : POTENTIAL;
                } else {
                    return TRUE;
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

