package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import htsjdk.variant.variantcontext.Allele;

import java.util.*;

import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArCompoundChecker {

    public Set<CompoundCheckResult> check(
            Map<GeneInfo, Set<VariantGeneRecord>> geneVariantMap,
            VariantGeneRecord variantGeneRecord, Pedigree family) {
        if (onAutosome(variantGeneRecord)) {
            Set<CompoundCheckResult> compounds = new HashSet<>();
            checkForGene(geneVariantMap, variantGeneRecord, family, compounds);
            return compounds;
        }
        return Collections.emptySet();
    }

    private void checkForGene(Map<GeneInfo, Set<VariantGeneRecord>> geneVariantMap,
                              VariantGeneRecord variantGeneRecord, Pedigree family, Set<CompoundCheckResult> compounds) {
        Collection<VariantGeneRecord> variantGeneRecords = geneVariantMap.get(variantGeneRecord.getGeneInfo());
        if (variantGeneRecords != null) {
            for (VariantGeneRecord otherRecord : variantGeneRecords) {
                if (!otherRecord.equals(variantGeneRecord)) {
                    MatchEnum isPossibleCompound = checkFamily(family, variantGeneRecord, otherRecord);
                    if (isPossibleCompound != FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherRecord).isCertain(isPossibleCompound != POTENTIAL).build();
                        compounds.add(result);
                    }
                }
            }
        }
    }

    private MatchEnum checkFamily(Pedigree family, VariantGeneRecord variantGeneRecord,
                                VariantGeneRecord otherVariantGeneRecord) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = getMembersByStatus(family);
        Set<List<Allele>> affectedGenotypes = new HashSet<>();
        Set<List<Allele>> otherAffectedGenotypes = new HashSet<>();
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkAffected(variantGeneRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        matches.add(checkUnaffected(variantGeneRecord, otherVariantGeneRecord, membersByStatus, affectedGenotypes, otherAffectedGenotypes));
        if(!membersByStatus.get(AffectedStatus.MISSING).isEmpty()){
            matches.add(POTENTIAL);
        }
        return merge(matches);
    }

    private MatchEnum checkUnaffected(VariantGeneRecord variantGeneRecord, VariantGeneRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            matches.add(checkUnaffectedSample(variantGeneRecord, otherVariantGeneRecord, affectedGenotypes, otherAffectedGenotypes, unAffectedSample));
        }
        return merge(matches);
    }

    private static MatchEnum checkUnaffectedSample(VariantGeneRecord variantGeneRecord, VariantGeneRecord otherVariantGeneRecord, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        matches.add(checkSingleUnaffectedSampleVariant(variantGeneRecord, affectedGenotypes, unAffectedSample));
        matches.add(checkSingleUnaffectedSampleVariant(otherVariantGeneRecord, otherAffectedGenotypes, unAffectedSample));
        if(matches.contains(TRUE)){
            return TRUE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }

        EffectiveGenotype sampleGt = variantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        EffectiveGenotype sampleOtherGt = otherVariantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        if(sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                (sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference())){
            return TRUE;
        }
        return FALSE;
    }

    private static MatchEnum checkSingleUnaffectedSampleVariant(VariantGeneRecord variantGeneRecord, Set<List<Allele>> affectedGenotypes, Sample unAffectedSample) {
        Set<MatchEnum> matches = new HashSet<>();
        EffectiveGenotype genotype = variantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        for (List<Allele> affectedGenotype : affectedGenotypes) {
            if(genotype != null && genotype.isHomRef()){
                matches.add(TRUE);
            }
            else if (affectedGenotype.stream().filter(Allele::isNonReference).allMatch(
                    allele -> allele.isCalled() && genotype!= null && genotype.getAlleles().contains(allele))) {
                matches.add(FALSE);
            } else{
                matches.add(POTENTIAL);
            }
        }
        return merge(matches);
    }

    private MatchEnum checkAffected(VariantGeneRecord variantGeneRecord, VariantGeneRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Set<List<Allele>> affectedGenotypes, Set<List<Allele>> otherAffectedGenotypes) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            EffectiveGenotype sampleGt = variantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            EffectiveGenotype sampleOtherGt = otherVariantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            if(sampleGt != null) {
                affectedGenotypes.add(sampleGt.getAlleles());
            }
            if(sampleOtherGt != null) {
                otherAffectedGenotypes.add(sampleOtherGt.getAlleles());
            }
            if((sampleGt != null && sampleGt.isHom()) || (sampleOtherGt != null && sampleOtherGt.isHom())){
                return FALSE;
            } else if ((sampleGt == null || !sampleGt.hasReference()) || (sampleOtherGt == null || !sampleOtherGt.hasReference())) {
                matches.add(POTENTIAL);
            } else{
                if(sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                        (sampleGt.getAllele(0).isReference() && sampleOtherGt.getAllele(0).isReference()) ||
                        (sampleGt.getAllele(1).isReference() && sampleOtherGt.getAllele(1).isReference())){
                    return FALSE;
                }
                matches.add(TRUE);
            }
        }
        return merge(matches);
    }

    private static MatchEnum merge(Set<MatchEnum> matches) {
        if (matches.contains(FALSE)) {
            return FALSE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private Map<AffectedStatus, Set<Sample>> getMembersByStatus(Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = new HashMap<>();
        Set<Sample> affected = new HashSet<>();
        Set<Sample> unAffected = new HashSet<>();
        Set<Sample> missing = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            if (sample.getPerson().getAffectedStatus() == AffectedStatus.AFFECTED) {
                affected.add(sample);
            } else if (sample.getPerson().getAffectedStatus() == AffectedStatus.UNAFFECTED) {
                unAffected.add(sample);
            } else {
                missing.add(sample);
            }
        }
        membersByStatus.put(AffectedStatus.AFFECTED, affected);
        membersByStatus.put(AffectedStatus.UNAFFECTED, unAffected);
        membersByStatus.put(AffectedStatus.MISSING, missing);
        return membersByStatus;
    }
}
