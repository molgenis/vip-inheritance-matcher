package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;

@Component
public class MtChecker extends InheritanceChecker {
    public MatchEnum check(
            VariantRecord variantRecord, Pedigree family) {
        if (!VariantContextUtils.onChromosomeMt(variantRecord)) {
            return FALSE;
        }

        return checkFamily(variantRecord, family);
    }

    protected MatchEnum checkUnaffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Allele> affectedAlleles) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            EffectiveGenotype genotype = variantRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if (genotype != null && (genotype.isHomRef() || (genotype.getPloidy() == 1 && genotype.hasReference()))) {
                matches.add(TRUE);
            } else if (genotype != null && genotype.getAlleles().stream().allMatch(
                    allele -> allele.isCalled() && affectedAlleles.contains(allele))) {
                matches.add(FALSE);
            } else {
                matches.add(POTENTIAL);
            }

        }
        return merge(matches);
    }

    protected MatchEnum checkAffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            EffectiveGenotype genotype = variantRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            affectedGenotypes.add(genotype);
            if (genotype != null && !genotype.hasAltAllele() && genotype.isCalled()) {
                return FALSE;
            } else if (genotype == null || genotype.isNoCall() || (genotype.isMixed())) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }
}
