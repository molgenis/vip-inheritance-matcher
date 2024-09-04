package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.Genotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

public abstract class HaploidChecker extends InheritanceChecker{
    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        Genotype genotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(genotype);
            }
            case UNAFFECTED -> {
                return checkUnaffected(genotype);
            }
            case MISSING -> {
                return POTENTIAL;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    protected MatchEnum checkUnaffected(Genotype genotype) {
        if (genotype.getPloidy() == 1) {
            return checkUnaffectedHaploid(genotype);
        } else if (genotype.getPloidy() == 2) {
            return checkUnaffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for haploid check''", genotype.getPloidy()));
        }
        return POTENTIAL;
    }

    protected MatchEnum checkUnaffectedDiploid(Genotype genotype) {
        if (genotype.hasAltAllele()|| genotype.isMixed()) {
            return genotype.isHom() ? FALSE : POTENTIAL;
        } else {
            return genotype.isHomRef() ? TRUE : POTENTIAL;
        }
    }

    protected MatchEnum checkUnaffectedHaploid(Genotype genotype) {
        if (genotype.isNoCall()) {
            return POTENTIAL;
        }
        if (genotype.hasAltAllele()) {
            return FALSE;
        }
        return TRUE;
    }

    protected MatchEnum checkAffected(Genotype genotype) {
        if (genotype.getPloidy() == 1) {
            return checkAffectedHaploid(genotype);
        }
        else if (genotype.getPloidy() == 2) {
            return checkAffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for YL check", genotype.getPloidy()));
        }
        return POTENTIAL;
    }

    protected MatchEnum checkAffectedDiploid(Genotype genotype) {
        if (genotype.hasAltAllele()) {
            return genotype.isHom() ? TRUE : POTENTIAL;
        } else if (genotype.isNoCall() || genotype.isMixed()) {
            return POTENTIAL;
        } else {
            return genotype.isHomRef() ? FALSE : POTENTIAL;
        }
    }

    protected MatchEnum checkAffectedHaploid(Genotype genotype) {
        if (genotype.isNoCall()) {
            return POTENTIAL;
        }
        if (genotype.hasAltAllele()) {
            return TRUE;
        }
        return FALSE;
    }
}
