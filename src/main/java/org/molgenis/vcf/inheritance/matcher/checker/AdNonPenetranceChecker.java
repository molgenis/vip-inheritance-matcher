package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AdNonPenetranceChecker {

    public MatchEnum check(
            VcfRecord vcfRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(vcfRecord)) {
            return FALSE;
        }

        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, vcfRecord));
        }
        return CheckerUtils.merge(results);
    }

    MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        Genotype sampleGt = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
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

