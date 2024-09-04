package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.Genotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

/**
 * Autosomal dominant (AD) inheritance pattern matcher
 */
@Component
public class AdChecker extends InheritanceChecker{
    /**
     * Check whether the AD inheritance pattern could match for a variant in a pedigree
     */
    public MatchEnum check(
            VcfRecord vcfRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(vcfRecord)) {
            return FALSE;
        }

        return checkFamily(vcfRecord, family);
    }

    MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        Genotype sampleGt = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        if (sampleGt == null || sampleGt.isNoCall()) {
            return POTENTIAL;
        } else {
            if (sampleGt.isMixed()) {
                return checkMixed(sample, sampleGt);
            } else {
                if (sampleGt.hasAltAllele()) {
                    return checkSampleWithVariant(sample);
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

    private static MatchEnum checkSampleWithVariant(Sample sample) {
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> TRUE;
            case UNAFFECTED -> FALSE;
            case MISSING -> POTENTIAL;
        };
    }

    private static MatchEnum checkMixed(Sample sample, Genotype sampleGt) {
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                if (!sampleGt.hasAltAllele()) {
                    return POTENTIAL;
                }
            }
            case UNAFFECTED -> {
                if (sampleGt.hasAltAllele()) {
                    return FALSE;
                } else {
                    return POTENTIAL;
                }
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
        return TRUE;
    }
}
