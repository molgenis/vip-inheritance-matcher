package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
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
            VariantRecord variantRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantRecord)) {
            return FALSE;
        }

        return checkFamily(variantRecord, family);
    }

    public MatchEnum checkUnaffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            EffectiveGenotype genotype = variantRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if(genotype == null){
                matches.add(POTENTIAL);
            }
            else if(genotype.isHomRef()){
                matches.add(TRUE);
            }
            else {
                checkAffectedGenotypes(affectedGenotypes, matches, genotype);
            }
        }
        return merge(matches);
    }

    public MatchEnum checkAffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            EffectiveGenotype genotype = variantRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            affectedGenotypes.add(genotype);
            if (genotype != null && genotype.isHomRef()) {
                return FALSE;
            } else if (genotype == null || (genotype.hasMissingAllele() && genotype.hasReference()) || genotype.isNoCall()) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }
}
