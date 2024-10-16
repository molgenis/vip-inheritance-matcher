package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
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

    MatchEnum checkFamily(VcfRecord vcfRecord, Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<Genotype> affectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(vcfRecord, membersByStatus, affectedGenotypes));
        matches.add(checkUnaffected(vcfRecord, membersByStatus, affectedGenotypes));
        if(!membersByStatus.get(AffectedStatus.MISSING).isEmpty()){
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    static void checkAffectedGenotypes(Set<Genotype> affectedGenotypes, Set<MatchEnum> matches, Genotype genotype) {
        for (Genotype affectedGenotype : affectedGenotypes) {
            if (affectedGenotype.hasAltAllele() && affectedGenotype.getAlleles().stream().filter(allele -> allele.isCalled() && allele.isNonReference()).allMatch(
                    allele -> genotype.getAlleles().contains(allele))) {
                matches.add(FALSE);
            } else {
                matches.add(POTENTIAL);
            }
        }
    }

    protected abstract MatchEnum checkUnaffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Genotype> affectedGenotypes);

    protected abstract MatchEnum checkAffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Genotype> affectedGenotypes);
}
