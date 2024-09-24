package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.getMembersByStatus;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

public abstract class DominantChecker {

    MatchEnum checkFamily(VariantRecord variantRecord, Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<EffectiveGenotype> affectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(variantRecord, membersByStatus, affectedGenotypes));
        matches.add(checkUnaffected(variantRecord, membersByStatus, affectedGenotypes));
        if(!membersByStatus.get(AffectedStatus.MISSING).isEmpty()){
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    static void checkAffectedGenotypes(Set<EffectiveGenotype> affectedGenotypes, Set<MatchEnum> matches, EffectiveGenotype genotype) {
        for (EffectiveGenotype affectedGenotype : affectedGenotypes) {
            if (affectedGenotype.hasAltAllele() && affectedGenotype.getAlleles().stream().filter(allele -> allele.isCalled() && allele.isNonReference()).allMatch(
                    allele -> genotype.getAlleles().contains(allele))) {
                matches.add(FALSE);
            } else {
                matches.add(POTENTIAL);
            }
        }
    }

    protected abstract MatchEnum checkUnaffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes);

    protected abstract MatchEnum checkAffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes);
}
