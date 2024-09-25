package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;

@Component
public class XldChecker extends DominantChecker  {

    public MatchEnum check(VcfRecord vcfRecord, Pedigree family) {
        if (!onChromosomeX(vcfRecord)) {
            return FALSE;
        }
        return checkFamily(vcfRecord, family);
    }

    public MatchEnum checkUnaffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Genotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            Genotype genotype = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
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

    public MatchEnum checkAffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Genotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            Genotype genotype = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
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
