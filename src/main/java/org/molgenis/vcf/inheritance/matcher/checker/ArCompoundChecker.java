package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.getMembersByStatus;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import htsjdk.variant.variantcontext.Allele;

import java.util.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArCompoundChecker {

    public Map<GeneInfo, Set<CompoundCheckResult>> check(
            Map<GeneInfo, Set<VariantRecord>> geneVariantMap,
            VariantRecord variantRecord, Pedigree family) {
        if (onAutosome(variantRecord)) {
            Map<GeneInfo, Set<CompoundCheckResult>> compounds = new HashMap<>();
            for (GeneInfo geneInfo : variantRecord.geneInfos()) {
                checkForGene(geneVariantMap, variantRecord, family, compounds, geneInfo);
            }
            return compounds;
        }
        return Collections.emptyMap();
    }

    private void checkForGene(Map<GeneInfo, Set<VariantRecord>> geneVariantMap,
                              VariantRecord variantRecord, Pedigree family, Map<GeneInfo, Set<CompoundCheckResult>> compoundsMap, GeneInfo geneInfo) {
        Collection<VariantRecord> variantGeneRecords = geneVariantMap.get(geneInfo);
        Set<CompoundCheckResult> compounds = new HashSet<>();
        if (variantGeneRecords != null) {
            for (VariantRecord otherRecord : variantGeneRecords) {
                if (!otherRecord.equals(variantRecord)) {
                    MatchEnum isPossibleCompound = checkFamily(family, variantRecord, otherRecord);
                    if (isPossibleCompound != FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherRecord).isCertain(isPossibleCompound != POTENTIAL).build();
                        compounds.add(result);
                    }
                }
            }
        }
        compoundsMap.put(geneInfo, compounds);
    }

    private MatchEnum checkFamily(Pedigree family, VariantRecord variantRecord,
                                  VariantRecord otherVariantGeneRecord) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<List<Allele>> affectedGenotypes = new HashSet<>();
        Set<List<Allele>> otherAffectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(variantRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        matches.add(checkUnaffected(variantRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        if (!membersByStatus.get(AffectedStatus.MISSING).isEmpty()) {
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    private MatchEnum checkUnaffected(VariantRecord variantRecord, VariantRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            matches.add(checkUnaffectedSample(variantRecord, otherVariantGeneRecord, affectedGenotypes, otherAffectedGenotypes, unAffectedSample));
        }
        return merge(matches);
    }

    private static MatchEnum checkUnaffectedSample(VariantRecord variantRecord, VariantRecord otherVariantGeneRecord, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkSingleUnaffectedSampleVariant(variantRecord, affectedGenotypes, unAffectedSample));
        matches.add(checkSingleUnaffectedSampleVariant(otherVariantGeneRecord, otherAffectedGenotypes, unAffectedSample));
        if (matches.contains(TRUE)) {
            return TRUE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }

        EffectiveGenotype sampleGt = variantRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        EffectiveGenotype sampleOtherGt = otherVariantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());

        if ((sampleGt != null && sampleOtherGt != null) && (sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                (sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference()))) {
            return TRUE;
        }
        return FALSE;
    }

    private static MatchEnum checkSingleUnaffectedSampleVariant(VariantRecord variantRecord, Set<List<Allele>> affectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        EffectiveGenotype genotype = variantRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        for (List<Allele> affectedGenotype : affectedGenotypes) {
            if (genotype != null && genotype.isHomRef()) {
                matches.add(TRUE);
            } else if (affectedGenotype.stream().filter(Allele::isNonReference).allMatch(
                    allele -> allele.isCalled() && genotype != null && genotype.getAlleles().contains(allele))) {
                matches.add(FALSE);
            } else {
                matches.add(POTENTIAL);
            }
        }
        return merge(matches);
    }

    private MatchEnum checkAffected(VariantRecord variantRecord, VariantRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            checkAffectedSample(variantRecord, otherVariantGeneRecord, affectedGenotypes, otherAffectedGenotypes, affectedSample, matches);
        }
        return merge(matches);
    }

    private static void checkAffectedSample(VariantRecord variantRecord, VariantRecord otherVariantGeneRecord, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes, Sample affectedSample, Set<MatchEnum> matches) {
        EffectiveGenotype sampleGt = variantRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        EffectiveGenotype sampleOtherGt = otherVariantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        if (sampleGt != null) {
            affectedGenotypes.add(sampleGt.getAlleles());
        }
        if (sampleOtherGt != null) {
            otherAffectedGenotypes.add(sampleOtherGt.getAlleles());
        }
        if ((sampleGt != null && sampleGt.isHom()) || (sampleOtherGt != null && sampleOtherGt.isHom())) {
            matches.add(FALSE);
        } else if ((sampleGt == null || !sampleGt.hasReference()) || (sampleOtherGt == null || !sampleOtherGt.hasReference())) {
            matches.add(POTENTIAL);
        } else {
            if (checkAffectedPhased(sampleGt, sampleOtherGt)) {
                matches.add(FALSE);
            }
            matches.add(TRUE);
        }
    }

    private static boolean checkAffectedPhased(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt) {
        return sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                (sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference());
    }
}
