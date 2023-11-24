package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

public class XldChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VariantContext variantContext) {
        Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (genotype == null || !genotype.isCalled()) {
            return POTENTIAL;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                // Affected individuals have to be het. or hom. alt.
                if (hasVariant(genotype)) {
                    return TRUE;
                } else {
                    //homRef? then XLD==false, is any allele is missing than the match is "potential"
                    return genotype.isHomRef() ? FALSE : POTENTIAL;
                }
            }
            case UNAFFECTED -> {
                switch (getSex(sample.getPerson().getSex(), genotype)) {
                    case MALE -> {
                        // Healthy males cannot carry the variant
                        if (genotype.getAlleles().stream()
                                .allMatch(Allele::isReference)) {
                            return TRUE;
                        } else if (hasVariant(genotype)) {
                            return FALSE;
                        }
                        return POTENTIAL;
                    }
                    case FEMALE -> {
                        // Healthy females can carry the variant (because of X inactivation)
                        if (genotype.isMixed() && hasVariant(genotype)) {
                            return POTENTIAL;
                        }
                        return (genotype.isHet() || genotype.isMixed() || genotype.isHomRef()) ? TRUE : FALSE;
                    }
                    default -> throw new IllegalArgumentException();
                }
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
