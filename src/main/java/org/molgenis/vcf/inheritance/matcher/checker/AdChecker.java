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

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

/**
 * Autosomal dominant (AD) inheritance pattern matcher
 */
@Component
public class AdChecker {
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

    private MatchEnum checkFamily(VariantGeneRecord variantGeneRecord, Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<EffectiveGenotype> affectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(variantGeneRecord, membersByStatus, affectedGenotypes));
        matches.add(checkUnaffected(variantGeneRecord, membersByStatus, affectedGenotypes));
        if(!membersByStatus.get(AffectedStatus.MISSING).isEmpty()){
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    private static MatchEnum checkUnaffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
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

    private static MatchEnum checkAffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
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

    private static MatchEnum merge(Set<MatchEnum> matches) {
        if (matches.contains(FALSE)) {
            return FALSE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private Map<AffectedStatus, Set<Sample>> getMembersByStatus(Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = new HashMap<>();
        Set<Sample> affected = new HashSet<>();
        Set<Sample> unAffected = new HashSet<>();
        Set<Sample> missing = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            if (sample.getPerson().getAffectedStatus() == AffectedStatus.AFFECTED) {
                affected.add(sample);
            } else if (sample.getPerson().getAffectedStatus() == AffectedStatus.UNAFFECTED) {
                unAffected.add(sample);
            } else {
                missing.add(sample);
            }
        }
        membersByStatus.put(AffectedStatus.AFFECTED, affected);
        membersByStatus.put(AffectedStatus.UNAFFECTED, unAffected);
        membersByStatus.put(AffectedStatus.MISSING, missing);
        return membersByStatus;
    }
}
