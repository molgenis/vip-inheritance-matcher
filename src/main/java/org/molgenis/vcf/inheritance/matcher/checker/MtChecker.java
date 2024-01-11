package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeMt;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

@Component
public class MtChecker extends InheritanceChecker {
    public MatchEnum check(VariantContext variantContext, Pedigree family) {
        if (!onChromosomeMt(variantContext)) {
            return FALSE;
        }
        return checkFamily(variantContext, family);
    }

    protected MatchEnum checkSample(Sample sample, VariantContext variantContext) {
        Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (genotype == null || !genotype.isCalled()) {
            return POTENTIAL;
        }

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

    private MatchEnum checkUnaffected(Genotype genotype) {
        if (genotype.getPloidy() == 1) {
            return checkUnaffectedHaploid(genotype);
        } else if (genotype.getPloidy() == 2) {
            return checkUnaffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for MT check", genotype.getPloidy()));
        }
        return null;
    }

    private MatchEnum checkUnaffectedDiploid(Genotype genotype) {
        if (hasVariant(genotype)) {
            return genotype.isHom() ? FALSE : POTENTIAL;
        } else {
            return genotype.isHomRef() ? TRUE : POTENTIAL;
        }
    }

    private MatchEnum checkUnaffectedHaploid(Genotype genotype) {
        if (genotype.isNoCall()) {
            return POTENTIAL;
        }
        if (genotype.hasAltAllele()) {
            return FALSE;
        }
        return TRUE;
    }

    private MatchEnum checkAffected(Genotype genotype) {
        if (genotype.getPloidy() == 1) {
            return checkAffectedHaploid(genotype);
        } else if (genotype.getPloidy() == 2) {
            return checkAffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for MT check", genotype.getPloidy()));
        }
        return POTENTIAL;
    }

    private MatchEnum checkAffectedDiploid(Genotype genotype) {
        if (hasVariant(genotype)) {
            return genotype.isHom() ? TRUE : POTENTIAL;
        } else {
            return genotype.isHomRef() ? FALSE : POTENTIAL;
        }
    }

    private MatchEnum checkAffectedHaploid(Genotype genotype) {
        if (genotype.isNoCall()) {
            return POTENTIAL;
        }
        if (genotype.hasAltAllele()) {
            return TRUE;
        }
        return FALSE;
    }
}
