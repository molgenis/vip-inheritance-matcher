package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ArChecker extends InheritanceChecker {

    public MatchEnum check(
            VariantRecord variantRecord, Pedigree family) {
        if (!VariantContextUtils.onAutosome(variantRecord)) {
            return FALSE;
        }

        return checkFamily(variantRecord, family);
    }

    protected MatchEnum checkUnaffected(VariantRecord variantRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Allele> affectedAlleles) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            EffectiveGenotype genotype = variantRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if(genotype == null){
                matches.add(POTENTIAL);
            } else if (genotype.hasReference()) {
                matches.add(TRUE);
            } else if (genotype.getAlleles().stream().allMatch(
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
            if (genotype!= null && genotype.hasReference()) {
                return FALSE;
            } else if (genotype == null || genotype.isMixed() || genotype.isNoCall()) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }
}
