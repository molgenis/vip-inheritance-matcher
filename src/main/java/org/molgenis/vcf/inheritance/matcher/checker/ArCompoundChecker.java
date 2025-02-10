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
    enum Classification {PATHOGENIC, UNKNOWN, BENIGN, CONFLICT}

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
        Map<Allele, Classification> alleleClassificationMap = new HashMap<>();
        calculateAlleleClasses(membersByStatus, vcfRecord, alleleClassificationMap);
        Map<Allele, Classification> otherAlleleClassificationMap = new HashMap<>();
        calculateAlleleClasses(membersByStatus, otherVariantGeneRecord, otherAlleleClassificationMap);
        Set<MatchEnum> matches = new HashSet<>();
        if (alleleClassificationMap.containsValue(Classification.CONFLICT) || otherAlleleClassificationMap.containsValue(Classification.CONFLICT)) {
            return FALSE;
        }
        matches.add(checkAffected(vcfRecord, otherVariantGeneRecord, membersByStatus, alleleClassificationMap, otherAlleleClassificationMap));
        matches.add(checkUnaffected(vcfRecord, otherVariantGeneRecord, membersByStatus, alleleClassificationMap, otherAlleleClassificationMap));
        if (!membersByStatus.get(AffectedStatus.MISSING).isEmpty()) {
            matches.add(POTENTIAL);
        }
        MatchEnum mergedResult = merge(matches);
        if (!checkHomozygoteVariants(vcfRecord, otherVariantGeneRecord, membersByStatus)) {
            return FALSE;
        }
        return mergedResult;
    }

    //check if for any genotype of an affected sample both alleles are present as homozygote variant in unaffected members
    private boolean checkHomozygoteVariants(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus) {
        return checkRecord(vcfRecord, membersByStatus) && checkRecord(otherVariantGeneRecord, membersByStatus);
    }

    //if both alleles of the genotype of an affected sample are seen as homozygote in an unaffected member
    //therefor it can not be a match.
    private boolean checkRecord(VcfRecord vcfRecord, Map<AffectedStatus, Set<Sample>> membersByStatus) {
        Set<Genotype> affectedGenotypes = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            Genotype sampleGt = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            affectedGenotypes.add(sampleGt);
        }
        for (Genotype genotype : affectedGenotypes) {
            for (Allele allele : genotype.getAlleles()) {
                boolean isSeenHom = false;
                for (Sample unaffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
                    Genotype sampleGt = vcfRecord.getGenotype(unaffectedSample.getPerson().getIndividualId());
                    if (allele.isCalled() && allele.isNonReference() && sampleGt != null && sampleGt.isCalled() && !sampleGt.isMixed() && sampleGt.isHom(allele)) {
                        isSeenHom = true;
                        break;
                    }
                }
                if (!isSeenHom) {
                    return true;
                }
            }
        }
        return false;
    }

    //determine based on affected samples which alleles have to be pathogenic for this variant to be a possible compound
    //alleles are considered pathogenic if the other allele of the same GT is reference or if the GT has 2 alternative alleles of which one has to be benign based on other samples
    //alternative alleles are considered benign if the GT has 2 alternative alleles of which one has to be benign based on other samples
    //if based on these rules a conflict arises, a variant that has to be bot benign and pathogenic this variant cannot be a compound hetrozygote
    private void calculateAlleleClasses(Map<AffectedStatus, Set<Sample>> membersByStatus, VcfRecord vcfRecord, Map<Allele, Classification> alleleClassificationMap) {
        alleleClassificationMap.put(vcfRecord.getReference(), Classification.BENIGN);
        if (vcfRecord.getAlternateAlleles().size() == 1) {
            alleleClassificationMap.put(vcfRecord.getAlternateAlleles().get(0), Classification.PATHOGENIC);
        } else {
            for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
                Genotype sampleGt = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
                if (sampleGt != null) {
                    processAlleleClassifications(alleleClassificationMap, sampleGt);
                }
            }
            reprocessAlleleClassifications(membersByStatus, vcfRecord, alleleClassificationMap);
        }
    }

    private static void processAlleleClassifications(Map<Allele, Classification> alleleClassificationMap, Genotype sampleGt) {
        if (sampleGt.isHet() && sampleGt.hasReference()) {
            sampleGt.getAlleles().forEach(allele -> {
                if (!allele.isReference() && allele.isCalled()) {
                    if ((alleleClassificationMap.get(allele) == Classification.BENIGN || alleleClassificationMap.get(allele) == Classification.CONFLICT)) {
                        alleleClassificationMap.put(allele, Classification.CONFLICT);
                    } else {
                        alleleClassificationMap.put(allele, Classification.PATHOGENIC);
                    }
                }
            });
        } else {
            addMissingAlleleClassification(alleleClassificationMap, sampleGt);
        }
    }

    private static void addMissingAlleleClassification(Map<Allele, Classification> alleleClassificationMap, Genotype sampleGt) {
        sampleGt.getAlleles().forEach(allele -> {
            if (!allele.isReference() && allele.isCalled() && !alleleClassificationMap.containsKey(allele)) {
                alleleClassificationMap.put(allele, Classification.UNKNOWN);
            }
        });
    }

    private static void reprocessAlleleClassifications(Map<AffectedStatus, Set<Sample>> membersByStatus, VcfRecord vcfRecord, Map<Allele, Classification> alleleClassificationMap) {
        //reprocess all affected
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            Genotype sampleGt = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
            if (sampleGt != null && !sampleGt.hasReference() && sampleGt.getAlleles().size() == 2 && sampleGt.isCalled() && !sampleGt.isMixed()) {
                Allele allele1 = sampleGt.getAlleles().get(0);
                Allele allele2 = sampleGt.getAlleles().get(1);
                Classification allele1Class = alleleClassificationMap.get(allele1);
                Classification allele2Class = alleleClassificationMap.get(allele2);
                //re-evaluate the UNKNOWN Alleles
                //For a part of a compound it is not possible for both alleles to be benign or both alleles to be pathogenic
                reevaluateClass(allele1Class, allele2Class, alleleClassificationMap, allele1);
                reevaluateClass(allele2Class, allele1Class, alleleClassificationMap, allele2);
                reevaluateClass(allele1Class, allele2Class, alleleClassificationMap, allele1);
                reevaluateClass(allele2Class, allele1Class, alleleClassificationMap, allele2);
            }
        }
    }

    private static void reevaluateClass(Classification allele1Class, Classification allele2Class, Map<Allele, Classification> alleleClassificationMap, Allele allele) {
        if (allele1Class == Classification.UNKNOWN && allele2Class != Classification.UNKNOWN) {
            if (allele2Class == Classification.PATHOGENIC) {
                alleleClassificationMap.put(allele, Classification.BENIGN);
            } else if (allele2Class == Classification.BENIGN) {
                alleleClassificationMap.put(allele, Classification.PATHOGENIC);
            }
        }
    }

    private MatchEnum checkUnaffected(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample unAffectedSample : membersByStatus.get(AffectedStatus.UNAFFECTED)) {
            matches.add(checkUnaffectedSample(vcfRecord, otherVariantGeneRecord, alleleClassificationMap, otherAlleleClassificationMap, unAffectedSample));
        }
        return merge(matches);
    }

    private static MatchEnum checkUnaffectedSample(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord,
                                                   Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Sample unAffectedSample) {
        Genotype gt = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        Genotype otherGt = otherVariantGeneRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        if (gt == null || otherGt == null) {
            return POTENTIAL;
        }
        if (isGtBothPathogenicAlt(gt, alleleClassificationMap)
                || isGtBothPathogenicAlt(otherGt, otherAlleleClassificationMap)) {
            return FALSE;
        }
        if (hasBenign(gt, alleleClassificationMap) && hasBenign(otherGt, otherAlleleClassificationMap)) {
            if (isPhasedSameBlock(gt, otherGt) && gt.getPloidy() == 2 && otherGt.getPloidy() == 2) {
                return checkUnaffectedSamplePhased(alleleClassificationMap, otherAlleleClassificationMap, gt, otherGt);
            } else {
                if (isHomBenign(gt, alleleClassificationMap) || isHomBenign(otherGt, otherAlleleClassificationMap)) {
                    return TRUE;
                }
                return POTENTIAL;
            }
        }
        return POTENTIAL;
    }

    private static MatchEnum checkUnaffectedSamplePhased(Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Genotype gt, Genotype otherGt) {
        Allele allele0 = gt.getAllele(0);
        Allele allele1 = gt.getAllele(1);
        Allele otherAllele0 = otherGt.getAllele(0);
        Allele otherAllele1 = otherGt.getAllele(1);
        //at least one allele fully benign -> match
        if (
                bothAllelesBenign(alleleClassificationMap, otherAlleleClassificationMap, allele0, otherAllele0) ||
                        bothAllelesBenign(alleleClassificationMap, otherAlleleClassificationMap, allele1, otherAllele1)
        ) {
            return TRUE;
        }
        //both alleles contain a pathogenic variant -> no match
        else if (
                bothAllelesPathogenic(alleleClassificationMap, otherAlleleClassificationMap, allele0, otherAllele1) ||
                        bothAllelesPathogenic(alleleClassificationMap, otherAlleleClassificationMap, allele1, otherAllele0)
        ) {
            return FALSE;
        }
        //both of the above checks inconclusive? -> potential
        return POTENTIAL;
    }

    private MatchEnum checkAffected(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Map<AffectedStatus, Set<Sample>> membersByStatus, Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap) {
        Set<MatchEnum> matches = new HashSet<>();
        for (Sample affectedSample : membersByStatus.get(AffectedStatus.AFFECTED)) {
            matches.add(checkAffectedSample(vcfRecord, otherVariantGeneRecord, affectedSample, alleleClassificationMap, otherAlleleClassificationMap));
        }
        return merge(matches);
    }

    private static MatchEnum checkAffectedSample(VcfRecord vcfRecord, VcfRecord otherVariantGeneRecord, Sample affectedSample, Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap) {
        Genotype sampleGt = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        Genotype sampleOtherGt = otherVariantGeneRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        if (sampleGt == null || sampleOtherGt == null) {
            if ((sampleOtherGt != null && sampleOtherGt.isHom()) || (sampleGt != null && sampleGt.isHom())) {
                return FALSE;
            }
            return POTENTIAL;
        }
        //single variant cannot be homozygote or effectivly homozygote for a part of a compound
        else if (sampleGt.isHom() || sampleOtherGt.isHom() || isGtBothPathogenicAlt(sampleGt, alleleClassificationMap) || isGtBothPathogenicAlt(sampleOtherGt, otherAlleleClassificationMap)) {
            return FALSE;
        } else if (areBothGenotypesFullyCalled(sampleGt, sampleOtherGt)) {
            if (isPhasedSameBlock(sampleGt, sampleOtherGt)) {
                return checkAffectedSamplePhased(alleleClassificationMap, otherAlleleClassificationMap, sampleGt, sampleOtherGt);
            } else {
                return checkAffectedUnphased(alleleClassificationMap, otherAlleleClassificationMap, sampleGt, sampleOtherGt);
            }
        }
        return POTENTIAL;
    }

    private static MatchEnum checkAffectedUnphased(Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Genotype sampleGt, Genotype sampleOtherGt) {
        if ((hasPathogenic(sampleGt, alleleClassificationMap) && hasPathogenic(sampleOtherGt, otherAlleleClassificationMap)) &&
                (hasBenign(sampleGt, alleleClassificationMap) && hasBenign(sampleOtherGt, otherAlleleClassificationMap))) {
            return TRUE;
        }
        return POTENTIAL;
    }

    private static MatchEnum checkAffectedSamplePhased(Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Genotype gt, Genotype otherGt) {
        Allele allele0 = gt.getAllele(0);
        Allele allele1 = gt.getAllele(1);
        Allele otherAllele0 = otherGt.getAllele(0);
        Allele otherAllele1 = otherGt.getAllele(1);
        //at least one allele fully benign -> match
        if (
                bothAllelesBenign(alleleClassificationMap, otherAlleleClassificationMap, allele0, otherAllele0) ||
                        bothAllelesBenign(alleleClassificationMap, otherAlleleClassificationMap, allele1, otherAllele1)
        ) {
            return FALSE;
        }
        //both alleles contain a pathogenic variant -> no match
        else if (
                bothAllelesPathogenic(alleleClassificationMap, otherAlleleClassificationMap, allele0, otherAllele1) ||
                        bothAllelesPathogenic(alleleClassificationMap, otherAlleleClassificationMap, allele1, otherAllele0)
        ) {
            return TRUE;
        }
        //both of the above checks inconclusive? -> potential
        return POTENTIAL;
    }

    private static boolean areBothGenotypesFullyCalled(Genotype sampleGt, Genotype sampleOtherGt) {
        return sampleGt.isCalled() && sampleOtherGt.isCalled() && !sampleGt.isMixed() && !sampleOtherGt.isMixed();
    }

    private static boolean bothAllelesBenign(Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Allele allele, Allele otherAllele) {
        return alleleClassificationMap.get(allele) == Classification.BENIGN && otherAlleleClassificationMap.get(otherAllele) == Classification.BENIGN;
    }

    private static boolean bothAllelesPathogenic(Map<Allele, Classification> alleleClassificationMap, Map<Allele, Classification> otherAlleleClassificationMap, Allele allele, Allele otherAllele) {
        return alleleClassificationMap.get(allele) == Classification.PATHOGENIC && otherAlleleClassificationMap.get(otherAllele) == Classification.PATHOGENIC;
    }

    private static boolean hasBenign(Genotype gt, Map<Allele, Classification> alleleClassificationMap) {
        return gt.getAlleles().stream().anyMatch(allele -> alleleClassificationMap.containsKey(allele)
                && alleleClassificationMap.get(allele) == Classification.BENIGN);
    }

    private static boolean hasPathogenic(Genotype gt, Map<Allele, Classification> alleleClassificationMap) {
        return gt.getAlleles().stream().anyMatch(allele -> alleleClassificationMap.containsKey(allele)
                && alleleClassificationMap.get(allele) == Classification.PATHOGENIC);
    }

    private static boolean isPhasedSameBlock(Genotype sampleGt, Genotype sampleOtherGt) {
        if (sampleGt == null || sampleOtherGt == null) {
            return false;
        }
        String phasingBlock = sampleGt.getPhasingBlock();
        String otherPhasingBlock = sampleOtherGt.getPhasingBlock();
        return (sampleGt.isPhased() && sampleOtherGt.isPhased() &&
                (phasingBlock != null && otherPhasingBlock != null) && phasingBlock.equals(otherPhasingBlock));
    }

    private static boolean isGtBothPathogenicAlt(Genotype genotype, Map<Allele, Classification> alleleClassificationMap) {
        return genotype != null && genotype.isHom() && alleleClassificationMap.containsKey(genotype.getAllele(0)) && alleleClassificationMap.get(genotype.getAllele(0)) == Classification.PATHOGENIC;
    }

    private static boolean isHomBenign(Genotype genotype, Map<Allele, Classification> alleleClassificationMap) {
        return alleleClassificationMap.containsKey(genotype.getAllele(0)) && alleleClassificationMap.get(genotype.getAllele(0)) == Classification.BENIGN &&
                alleleClassificationMap.containsKey(genotype.getAllele(1)) && alleleClassificationMap.get(genotype.getAllele(1)) == Classification.BENIGN;
    }
}
