package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class XldChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        EffectiveGenotype effectiveGenotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        if (effectiveGenotype == null || !effectiveGenotype.isCalled()) {
            return POTENTIAL;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(effectiveGenotype);
            }
            case UNAFFECTED -> {
                return checkUnaffected(sample, effectiveGenotype);
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private MatchEnum checkUnaffected(Sample sample, EffectiveGenotype effectiveGenotype) {
        switch (getSex(sample.getPerson().getSex(), effectiveGenotype)) {
            case MALE -> {
                // Healthy males cannot carry the variant
                if (effectiveGenotype.getAlleles().stream()
                        .allMatch(Allele::isReference)) {
                    return TRUE;
                } else if (effectiveGenotype.hasAltAllele()) {
                    return FALSE;
                }
                return POTENTIAL;
            }
            case FEMALE -> {
                // Healthy females can carry the variant (because of X inactivation)
                if (effectiveGenotype.isMixed() && effectiveGenotype.hasAltAllele()) {
                    return POTENTIAL;
                }
                return (effectiveGenotype.isHet() || effectiveGenotype.isMixed() || effectiveGenotype.isHomRef()) ? TRUE : FALSE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private static MatchEnum checkAffected(EffectiveGenotype effectiveGenotype) {
        // Affected individuals have to be het. or hom. alt.
        if (effectiveGenotype.hasAltAllele()) {
            return TRUE;
        } else {
            //homRef? then XLD==false, is any allele is missing than the match is "potential"
            return effectiveGenotype.isHomRef() ? FALSE : POTENTIAL;
        }
    }
}
