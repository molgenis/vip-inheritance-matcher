package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;

@Component
public class XldChecker extends DominantChecker  {

    public MatchEnum check(VariantRecord variantRecord, Pedigree family) {
        if (!onChromosomeX(variantRecord)) {
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
            else if(!genotype.hasAltAllele() && !genotype.isMixed() || (genotype.hasReference() && genotype.getPloidy() == 2)){
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
            if (genotype != null && !genotype.hasAltAllele() && !genotype.isMixed()) {
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
