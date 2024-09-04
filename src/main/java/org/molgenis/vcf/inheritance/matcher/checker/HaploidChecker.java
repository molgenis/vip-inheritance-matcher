package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

public abstract class HaploidChecker extends InheritanceChecker{
    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        EffectiveGenotype effectiveGenotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(effectiveGenotype);
            }
            case UNAFFECTED -> {
                return checkUnaffected(effectiveGenotype);
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    protected MatchEnum checkUnaffected(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.getPloidy() == 1) {
            return checkUnaffectedHaploid(effectiveGenotype);
        } else if (effectiveGenotype.getPloidy() == 2) {
            return checkUnaffectedDiploid(effectiveGenotype);
        } else if (effectiveGenotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for haploid check''", effectiveGenotype.getPloidy()));
        }
        return POTENTIAL;
    }

    protected MatchEnum checkUnaffectedDiploid(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.hasAltAllele()|| effectiveGenotype.isMixed()) {
            return effectiveGenotype.isHom() ? FALSE : POTENTIAL;
        } else {
            return effectiveGenotype.isHomRef() ? TRUE : POTENTIAL;
        }
    }

    protected MatchEnum checkUnaffectedHaploid(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.isNoCall()) {
            return POTENTIAL;
        }
        if (effectiveGenotype.hasAltAllele()) {
            return FALSE;
        }
        return TRUE;
    }

    protected MatchEnum checkAffected(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.getPloidy() == 1) {
            return checkAffectedHaploid(effectiveGenotype);
        }
        else if (effectiveGenotype.getPloidy() == 2) {
            return checkAffectedDiploid(effectiveGenotype);
        } else if (effectiveGenotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for YL check", effectiveGenotype.getPloidy()));
        }
        return POTENTIAL;
    }

    protected MatchEnum checkAffectedDiploid(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.hasAltAllele()) {
            return effectiveGenotype.isHom() ? TRUE : POTENTIAL;
        } else if (effectiveGenotype.isNoCall() || effectiveGenotype.isMixed()) {
            return POTENTIAL;
        } else {
            return effectiveGenotype.isHomRef() ? FALSE : POTENTIAL;
        }
    }

    protected MatchEnum checkAffectedHaploid(EffectiveGenotype effectiveGenotype) {
        if (effectiveGenotype.isNoCall()) {
            return POTENTIAL;
        }
        if (effectiveGenotype.hasAltAllele()) {
            return TRUE;
        }
        return FALSE;
    }
}
