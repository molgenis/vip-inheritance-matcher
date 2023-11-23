package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.HashSet;
import java.util.Set;

import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResult;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceResult.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

/**
 * Autosomal dominant (AD) inheritance pattern matcher
 */
public class AdChecker {

    private AdChecker() {
    }

    /**
     * Check whether the AD inheritance pattern could match for a variant in a pedigree
     */
    public static InheritanceResult check(
            VariantContext variantContext, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantContext)) {
            return FALSE;
        }

        return checkFamily(variantContext, family);
    }

    public static InheritanceResult checkFamily(VariantContext variantContext, Pedigree family) {
        Set<InheritanceResult> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantContext));
        }
        if(results.contains(FALSE)){
            return FALSE;
        }else if(results.contains(POTENTIAL)){
            return POTENTIAL;
        }
        return TRUE;
    }

    private static InheritanceResult checkSample(Sample sample, VariantContext variantContext) {
        Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (sampleGt == null || sampleGt.isNoCall()) {
            return POTENTIAL;
        } else {
            if (sampleGt.isMixed()) {
                return checkMixed(sample, sampleGt);
            } else {
                if (hasVariant(sampleGt)) {
                    return checkSampleWithVariant(sample);
                } else {
                    return checkSampleWithoutVariant(sample);
                }
            }
        }
    }

    private static InheritanceResult checkSampleWithoutVariant(Sample sample) {
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> FALSE;
            case UNAFFECTED -> TRUE;
            case MISSING -> POTENTIAL;
        };
    }

    private static InheritanceResult checkSampleWithVariant(Sample sample) {
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> TRUE;
            case UNAFFECTED -> FALSE;
            case MISSING -> POTENTIAL;
        };
    }

    private static InheritanceResult checkMixed(Sample sample, Genotype sampleGt) {
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                if (!hasVariant(sampleGt)) {
                    return POTENTIAL;
                }
            }
            case UNAFFECTED -> {
                if (hasVariant(sampleGt)) {
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
