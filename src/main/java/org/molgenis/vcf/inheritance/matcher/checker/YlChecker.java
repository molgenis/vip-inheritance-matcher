package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeY;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

@Component
public class YlChecker {
    public MatchEnum check(VariantContext variantContext, Pedigree family) {
        if (!onChromosomeY(variantContext)) {
            return FALSE;
        }
        return checkFamily(variantContext, family);
    }

    public MatchEnum checkFamily(VariantContext variantContext, Pedigree family) {
        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            if (sample.getPerson().getSex() == Sex.FEMALE) {
                //female familty members do not play a role in Y-linked inheritance
                results.add(TRUE);
            }
            results.add(checkSample(sample, variantContext));
        }
        if (results.contains(FALSE)) {
            return FALSE;
        } else if (results.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
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
            checkUnaffectedHaploid(genotype);
        } else if (genotype.getPloidy() == 2) {
            checkUnaffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for YL check", genotype.getPloidy()));
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
        }
        else if (genotype.getPloidy() == 2) {
            return checkAffectedDiploid(genotype);
        } else if (genotype.isCalled()) {
            throw new UnsupportedOperationException(String.format("Incompatible ploidy '%s' for YL check", genotype.getPloidy()));
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
