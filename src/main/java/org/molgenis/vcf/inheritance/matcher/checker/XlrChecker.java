package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class XlrChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        EffectiveGenotype effectiveGenotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        if (effectiveGenotype == null || !effectiveGenotype.isCalled()) {
            return POTENTIAL;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(sample, effectiveGenotype);
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
                // Healthy males cannot carry the variant.
                if (effectiveGenotype.hasAltAllele()) {
                    return FALSE;
                } else if (effectiveGenotype.isHomRef()) {
                    return TRUE;
                }
                return null;
            }
            case FEMALE -> {
                // Healthy females cannot be hom. alt.
                if (effectiveGenotype.hasAltAllele() && effectiveGenotype.isHom()) {
                    return FALSE;
                } else if (effectiveGenotype.hasAltAllele() && effectiveGenotype.isMixed()) {
                    return POTENTIAL;
                }
                return TRUE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private MatchEnum checkAffected(Sample sample, EffectiveGenotype effectiveGenotype) {
        switch (getSex(sample.getPerson().getSex(), effectiveGenotype)) {
            case MALE -> {
                // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
                if (effectiveGenotype.hasAltAllele()) {
                    return TRUE;
                } else if (effectiveGenotype.isMixed()) {
                    return POTENTIAL;
                }
                return FALSE;
            }
            case FEMALE -> {
                // Affected females have to be hom. alt.
                if (effectiveGenotype.isHomRef()) {
                    return FALSE;
                } else if (effectiveGenotype.hasAltAllele() && effectiveGenotype.isMixed()) {
                    return POTENTIAL;
                }
                return effectiveGenotype.isHom() ? TRUE : FALSE;
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
