package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
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

public abstract class InheritanceChecker {

    protected MatchEnum checkFamily(VariantRecord variantRecord, Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<EffectiveGenotype> affectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(variantRecord, membersByStatus, affectedGenotypes));
        Set<Allele> affectedAltAlleles = new HashSet<>();
        affectedGenotypes.forEach(genotype -> genotype.getAlleles().stream().filter(allele -> allele.isNonReference() && allele.isCalled()).forEach(affectedAltAlleles::add));
        matches.add(checkUnaffected(variantRecord, membersByStatus, affectedAltAlleles));
        if (!membersByStatus.get(AffectedStatus.MISSING).isEmpty()) {
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    protected abstract MatchEnum checkAffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes);

    protected abstract MatchEnum checkUnaffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Allele> affectedAltAlleles);
}
