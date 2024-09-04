package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.Genotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class XldChecker extends XlChecker {

    protected MatchEnum checkSample(Sample sample, VcfRecord vcfRecord) {
        Genotype genotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        if (genotype == null || !genotype.isCalled()) {
            return POTENTIAL;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> {
                return checkAffected(genotype);
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
                // Healthy males cannot carry the variant
                if (genotype.getAlleles().stream()
                        .allMatch(Allele::isReference)) {
                    return TRUE;
                } else if (genotype.hasAltAllele()) {
                    return FALSE;
                }
                return POTENTIAL;
            }
            case FEMALE -> {
                // Healthy females can carry the variant (because of X inactivation)
                if (genotype.isMixed() && genotype.hasAltAllele()) {
                    return POTENTIAL;
                }
                return (genotype.isHet() || genotype.isMixed() || genotype.isHomRef()) ? TRUE : FALSE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private static MatchEnum checkAffected(Genotype genotype) {
        // Affected individuals have to be het. or hom. alt.
        if (genotype.hasAltAllele()) {
            return TRUE;
        } else {
            //homRef? then XLD==false, is any allele is missing than the match is "potential"
            return genotype.isHomRef() ? FALSE : POTENTIAL;
        }
    }
}
