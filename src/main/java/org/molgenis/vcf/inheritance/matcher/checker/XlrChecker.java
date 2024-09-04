package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.Genotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class XlrChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        Genotype genotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
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
                if (genotype.hasAltAllele()) {
                    return FALSE;
                } else if (genotype.isHomRef()) {
                    return TRUE;
                }
                return null;
            }
            case FEMALE -> {
                // Healthy females cannot be hom. alt.
                if (genotype.hasAltAllele() && genotype.isHom()) {
                    return FALSE;
                } else if (genotype.hasAltAllele() && genotype.isMixed()) {
                    return POTENTIAL;
                }
                return TRUE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private MatchEnum checkAffected(Sample sample, Genotype genotype) {
        switch (getSex(sample.getPerson().getSex(), genotype)) {
            case MALE -> {
                // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
                if (genotype.hasAltAllele()) {
                    return TRUE;
                } else if (genotype.isMixed()) {
                    return POTENTIAL;
                }
                return FALSE;
            }
            case FEMALE -> {
                // Affected females have to be hom. alt.
                if (genotype.isHomRef()) {
                    return FALSE;
                } else if (genotype.hasAltAllele() && genotype.isMixed()) {
                    return POTENTIAL;
                }
                return genotype.isHom() ? TRUE : FALSE;
            }
            default -> throw new IllegalArgumentException();
        }
    }
}
