package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

public class XlrChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VariantContext variantContext) {
        Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (genotype == null || !genotype.isCalled()) {
            return POTENTIAL;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(sample, genotype);
            }
            case UNAFFECTED -> {
                return checkUnaffected(sample, genotype);
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private MatchEnum checkUnaffected(Sample sample, Genotype genotype) {
        switch (getSex(sample.getPerson().getSex(), genotype)) {
            case MALE -> {
                // Healthy males cannot carry the variant.
                if (hasVariant(genotype)) {
                    return FALSE;
                } else if (genotype.isHomRef()) {
                    return TRUE;
                }
                return null;
            }
            case FEMALE -> {
                // Healthy females cannot be hom. alt.
                if (hasVariant(genotype) && genotype.isHom()) {
                    return FALSE;
                } else if (hasVariant(genotype) && genotype.isMixed()) {
                    return POTENTIAL;
                }
                return TRUE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private MatchEnum checkAffected(Sample sample, Genotype genotype) {
        switch (getSex(sample.getPerson().getSex(), genotype)) {
            case MALE:
                // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
                if (hasVariant(genotype)) {
                    return TRUE;
                } else if (genotype.isMixed()) {
                    return POTENTIAL;
                }
                return FALSE;
            case FEMALE:
                // Affected females have to be hom. alt.
                if (genotype.isHomRef()) {
                    return FALSE;
                } else if (hasVariant(genotype) && genotype.isMixed()) {
                    return POTENTIAL;
                }
                return genotype.isHom() ? TRUE : FALSE;
            default:
                throw new IllegalArgumentException();
        }
    }
}
