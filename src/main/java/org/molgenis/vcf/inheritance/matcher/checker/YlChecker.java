package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.utils.sample.model.Sex.FEMALE;
import static org.molgenis.vcf.utils.sample.model.Sex.MALE;

@Component
public class YlChecker extends InheritanceChecker {
    public MatchEnum check(
            VariantGeneRecord variantGeneRecord, Pedigree family) {
        if (!VariantContextUtils.onChromosomeY(variantGeneRecord)) {
            return FALSE;
        }

        return checkFamily(variantGeneRecord, family);
    }

    protected MatchEnum checkUnaffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Allele> affectedAlleles) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            EffectiveGenotype genotype = variantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if(getSex(unAffectedSample, genotype) == FEMALE){
                matches.add(TRUE);
            } else if (genotype.isHomRef() || (genotype.getPloidy() == 1 && genotype.hasReference())) {
                matches.add(TRUE);
            }
            else {
                    if (genotype.getAlleles().stream().allMatch(
                            allele -> allele.isCalled() && affectedAlleles.contains(allele))) {
                        matches.add(FALSE);
                    } else {
                        matches.add(POTENTIAL);
                    }
            }
        }
        return merge(matches);
    }

    protected MatchEnum checkAffected(VariantGeneRecord variantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<EffectiveGenotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            EffectiveGenotype genotype = variantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            if(getSex(affectedSample, genotype) == FEMALE){
                return FALSE;
            }
            affectedGenotypes.add(genotype);
            if (!genotype.hasAlt() && genotype.isCalled()) {
                return FALSE;
            } else if (genotype.isNoCall() || (genotype.isMixed())) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }

    protected static Sex getSex(Sample sample, EffectiveGenotype genotype) {
        if (sample.getPerson().getSex() == Sex.UNKNOWN) {
            //UNKNOWN? use best guess based on number of alleles
            if (genotype.getAlleles().size() == 1) {
                return MALE;
            } else {
                return FEMALE;
            }
        }else{
            return sample.getPerson().getSex();
        }
    }

}
