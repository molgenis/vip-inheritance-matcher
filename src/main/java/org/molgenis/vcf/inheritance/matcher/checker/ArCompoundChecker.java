package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.getMembersByStatus;
import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import htsjdk.variant.variantcontext.Allele;

import java.util.*;

import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArCompoundChecker {

    public Map<GeneInfo, Set<CompoundCheckResult>> check(
            Map<GeneInfo, Set<VcfRecord>> geneVariantMap,
            VcfRecord vcfRecord, Pedigree family) {
        if (onAutosome(vcfRecord)) {
            Map<GeneInfo, Set<CompoundCheckResult>> compounds = new HashMap<>();
            for (GeneInfo geneInfo : vcfRecord.geneInfos()) {
                checkForGene(geneVariantMap, vcfRecord, family, compounds, geneInfo);
            }
            return compounds;
        }
        return Collections.emptyMap();
    }

    private void checkForGene(Map<GeneInfo, Set<VcfRecord>> geneVariantMap,
                              VcfRecord vcfRecord, Pedigree family, Map<GeneInfo, Set<CompoundCheckResult>> compoundsMap, GeneInfo geneInfo) {
        Collection<VcfRecord> variantGeneRecords = geneVariantMap.get(geneInfo);
        Set<CompoundCheckResult> compounds = new LinkedHashSet<>();
        if (variantGeneRecords != null) {
            for (VcfRecord otherRecord : variantGeneRecords) {
                if (!otherRecord.equals(vcfRecord)) {
                    MatchEnum isPossibleCompound = checkFamily(family, vcfRecord, otherRecord);
                    if (isPossibleCompound != FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherRecord).isCertain(isPossibleCompound != POTENTIAL).build();
                        compounds.add(result);
                    }
                }
            }
        }
        compoundsMap.put(geneInfo, compounds);
    }

    private MatchEnum checkFamily(Pedigree family, VcfRecord vcfRecord,
                                  VcfRecord otherVariantGeneRecord) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<List<Allele>> affectedGenotypes = new HashSet<>();
        Set<List<Allele>> otherAffectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(vcfRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        matches.add(checkUnaffected(vcfRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        if (!membersByStatus.get(AffectedStatus.MISSING).isEmpty()) {
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    private MatchEnum checkUnaffected(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            matches.add(checkUnaffectedSample(vcfRecord, otherVariantGeneRecord, affectedGenotypes, otherAffectedGenotypes, unAffectedSample));
        }
        return merge(matches);
    }

    private static MatchEnum checkUnaffectedSample(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        //None of the variants can be present hom_alt for an unaffected sample
        if(isHomAlt(vcfRecord, affectedGenotypes, unAffectedSample)|| isHomAlt(otherVariantGeneRecord , affectedGenotypes,unAffectedSample)){
            return FALSE;
        }
        //One of the variants should be a "match", meaning: the unaffected sample does not have it
        matches.add(checkSingleUnaffectedSampleVariant(vcfRecord, affectedGenotypes, unAffectedSample));
        matches.add(checkSingleUnaffectedSampleVariant(otherVariantGeneRecord, otherAffectedGenotypes, unAffectedSample));
        if (matches.contains(TRUE)) {
            return TRUE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }

        //if the both variants are hetrozygous present in an unaffected sample in phased data, check if both are on the same allele.
        Genotype sampleGt = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        Genotype sampleOtherGt = otherVariantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        if ((sampleGt != null && sampleOtherGt != null) && isPhasedSameBlock(sampleGt, sampleOtherGt) && (
                (sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                        (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference()))) {
            return TRUE;
        }
        return FALSE;
    }

    private static boolean isPhasedSameBlock(Genotype sampleGt, Genotype sampleOtherGt) {
        String phasingBlock = sampleGt.getPhasingBlock();
        String otherPhasingBlock = sampleOtherGt.getPhasingBlock();
        return (sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                (phasingBlock != null && otherPhasingBlock != null) && phasingBlock.equals(otherPhasingBlock));
    }

    private static boolean isHomAlt(VcfRecord vcfRecord, Set<List<Allele>> affectedGenotypes, Sample unAffectedSample) {
        Genotype genotype = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        if (genotype != null && !genotype.isHomRef() && !genotype.hasMissingAllele()) {
            Set<Allele> affectedAlts = new HashSet<>();
            for (List<Allele> affectedGenotype : affectedGenotypes) {
                affectedGenotype.stream().filter(Allele::isNonReference).forEach(affectedAlts::add);
            }
            //both alleles are an alternative allele that was seen in affected samples, we consider this "homAlt"
            return affectedAlts.containsAll(genotype.getAlleles());
        }
        return false;
    }

    private static MatchEnum checkSingleUnaffectedSampleVariant(VcfRecord vcfRecord, Set<List<Allele>> affectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        Genotype genotype = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
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

    private MatchEnum checkAffected(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            checkAffectedSample(vcfRecord, otherVariantGeneRecord, affectedGenotypes, otherAffectedGenotypes, affectedSample, matches);
        }
        return merge(matches);
    }

    private static void checkAffectedSample(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes, Sample affectedSample, Set<MatchEnum> matches) {
        Genotype sampleGt = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        Genotype sampleOtherGt = otherVariantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
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
            if (isPhasedSameBlock(sampleGt, sampleOtherGt) && !checkAffectedPhased(sampleGt, sampleOtherGt)) {
                matches.add(FALSE);
            }
            matches.add(TRUE);
        }
    }

    private static boolean checkAffectedPhased(Genotype sampleGt, Genotype sampleOtherGt) {
        return !((sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference()));
    }
}
