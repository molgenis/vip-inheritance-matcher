package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class XlrChecker extends InheritanceChecker {

    public MatchEnum check(VcfRecord vcfRecord, Pedigree family) {
        if (!onChromosomeX(vcfRecord)) {
            return FALSE;
        }
        return checkFamily(vcfRecord, family);
    }

    protected MatchEnum checkUnaffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Allele> affectedAlleles) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            Genotype genotype = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
            if (genotype != null && genotype.hasReference()) {
                matches.add(TRUE);
            } else if (genotype != null && genotype.getAlleles().stream().allMatch(
                    allele -> allele.isCalled() && allele.isNonReference() && affectedAlleles.contains(allele))) {
                matches.add(FALSE);
            } else {
                matches.add(POTENTIAL);
            }
        }
        return merge(matches);
    }

    protected MatchEnum checkAffected(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<Genotype> affectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            Genotype genotype = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            affectedGenotypes.add(genotype);
            if (genotype != null && genotype.hasReference()) {
                return FALSE;
            } else if ((genotype == null || genotype.hasMissingAllele()) || genotype.isNoCall()) {
                matches.add(POTENTIAL);
            } else {
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }
}
