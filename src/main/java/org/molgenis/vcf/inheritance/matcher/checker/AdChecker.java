package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.HashSet;
import java.util.Set;

import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

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
    public static Boolean check(
            VariantContext variantContext, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantContext)) {
            return false;
        }

        return checkFamily(variantContext, family);
    }

    public static Boolean checkFamily(VariantContext variantContext, Pedigree family) {
        Set<Boolean> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantContext));
        }
        if(results.contains(false)){
            return false;
        }else if(results.contains(null)){
            return null;
        }
        return true;
    }

    private static Boolean checkSample(Sample sample, VariantContext variantContext) {
        Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (sampleGt == null || sampleGt.isNoCall()) {
            return null;
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

    private static Boolean checkSampleWithoutVariant(Sample sample) {
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> false;
            case UNAFFECTED -> true;
            case MISSING -> null;
            default -> throw new IllegalArgumentException();
        };
    }

    private static Boolean checkSampleWithVariant(Sample sample) {
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED:
                return true;
            case UNAFFECTED:
                return false;
            case MISSING:
                return null;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static Boolean checkMixed(Sample sample, Genotype sampleGt) {
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                if (!hasVariant(sampleGt)) {
                    return null;
                }
            }
            case UNAFFECTED -> {
                if (hasVariant(sampleGt)) {
                    return false;
                } else {
                    return null;
                }
            }
            case MISSING -> {
                return null;
            }
            default -> throw new IllegalArgumentException();
        }
        return true;
    }
}
