package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

/**
 * Autosomal dominant (AD) inheritance pattern matcher
 */
@Component
public class AdChecker extends DominantChecker {
    /**
     * Check whether the AD inheritance pattern could match for a variant in a pedigree
     */
    public MatchEnum check(
            VariantGeneRecord variantGeneRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantGeneRecord)) {
            return FALSE;
        }

        return checkFamily(variantGeneRecord, family);
    }

    public MatchEnum checkUnaffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            EffectiveGenotype genotype = variantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if(genotype == null){
                matches.add(POTENTIAL);
            }
            else if(genotype.isHomRef()){
                matches.add(TRUE);
            }
            else {
                for (EffectiveGenotype affectedGenotype : affectedGenotypes) {
                    if (affectedGenotype.hasAlt() && affectedGenotype.getAlleles().stream().filter(allele -> allele.isCalled() && allele.isNonReference()).allMatch(
                            allele -> genotype.getAlleles().contains(allele))) {
                        matches.add(FALSE);
                    } else {
                        matches.add(POTENTIAL);
                    }
                }
            }
        }
        return merge(matches);
    }

    public MatchEnum checkAffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            EffectiveGenotype genotype = variantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            affectedGenotypes.add(genotype);
            if (genotype.isHomRef()) {
                return FALSE;
            } else if ((genotype.hasMissingAllele() && genotype.hasReference()) || genotype.isNoCall()) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }
}
