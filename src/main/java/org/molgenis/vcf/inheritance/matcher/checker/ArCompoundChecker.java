package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.isAlt;

import htsjdk.variant.variantcontext.Allele;

import java.util.*;

import htsjdk.variant.variantcontext.GenotypeBuilder;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArCompoundChecker {

    private final VepMapper vepMapper;

    public ArCompoundChecker(VepMapper vepMapper) {
        this.vepMapper = vepMapper;
    }

    public List<CompoundCheckResult> check(
            Map<String, List<VcfRecord>> geneVariantMap,
            VcfRecord vcfRecord, Pedigree family, MatchEnum isAr) {
        if (onAutosome(vcfRecord) && isAr != TRUE) {
            List<CompoundCheckResult> compounds = new ArrayList<>();
            Map<String, Gene> genes = vepMapper.getGenes(vcfRecord).getGenes();
            for (Gene gene : genes.values()) {
                checkForGene(geneVariantMap, vcfRecord, family, compounds, gene);
            }
            return compounds;
        }
        return Collections.emptyList();
    }

    private void checkForGene(Map<String, List<VcfRecord>> geneVariantMap,
                              VcfRecord vcfRecord, Pedigree family, List<CompoundCheckResult> compounds,
                              Gene gene) {
        List<VcfRecord> variantContexts = geneVariantMap.get(gene.getId());
        if (variantContexts != null) {
            for (VcfRecord otherRecord : variantContexts) {
                if (!otherRecord.equals(vcfRecord)) {
                    MatchEnum isPossibleCompound = checkFamily(family, vcfRecord, otherRecord);
                    if (isPossibleCompound != FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherRecord).isCertain(isPossibleCompound != POTENTIAL).build();
                        compounds.add(result);
                    }
                }
            }
        }
    }

    private MatchEnum checkFamily(Pedigree family, VcfRecord vcfRecord,
                                VcfRecord otherVcfRecord) {
        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, vcfRecord, otherVcfRecord));
        }
        if (results.contains(FALSE)) {
            return FALSE;
        } else if (results.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private MatchEnum checkSample(Sample sample, VcfRecord vcfRecord, VcfRecord otherVcfRecord) {
        //Affected individuals have to be het. for both variants
        //Healthy individuals can be het. for one of the variants but cannot have both variants

        EffectiveGenotype sampleGt = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
        EffectiveGenotype sampleOtherGt = otherVcfRecord.getGenotype(sample.getPerson().getIndividualId());
        sampleGt = sampleGt != null ? sampleGt : new EffectiveGenotype(GenotypeBuilder.createMissing(sample.getPerson().getIndividualId(), 2), vcfRecord.unwrap(), Collections.emptyList());
        sampleOtherGt = sampleOtherGt != null ? sampleOtherGt : new EffectiveGenotype(GenotypeBuilder.createMissing(sample.getPerson().getIndividualId(), 2), otherVcfRecord.unwrap(), Collections.emptyList());
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> checkAffectedSample(sampleGt, sampleOtherGt);
            case UNAFFECTED -> checkUnaffectedSample(sampleGt, sampleOtherGt);
            case MISSING -> POTENTIAL;
        };
    }

    private MatchEnum checkAffectedSample(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt) {
        if (sampleGt.isHomRef() || sampleOtherGt.isHomRef()) {
            return FALSE;
        } else if (sampleGt.isHomAlt()
                || sampleOtherGt.isHomAlt()) {
            return FALSE;
        } else if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkSamplePhased(sampleGt, sampleOtherGt, true);
        }
        return checkAffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private MatchEnum checkUnaffectedSample(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt) {
        if ((sampleGt.isNoCall() && (sampleOtherGt.hasAltAllele() || sampleOtherGt.isNoCall()))
                || sampleOtherGt.isNoCall() && (sampleGt.hasAltAllele())) {
            return POTENTIAL;
        } else if (sampleGt.isHomAlt() || sampleOtherGt.isHomAlt()) {
            return FALSE;
        } else if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkSamplePhased(sampleGt, sampleOtherGt, false);
        }
        return checkUnaffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private MatchEnum checkUnaffectedSampleUnphased(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt) {
        boolean sampleContainsAlt = sampleGt.hasAltAllele();
        boolean sampleOtherGtContainsAlt = sampleOtherGt.hasAltAllele();
        if (sampleContainsAlt && sampleOtherGtContainsAlt) {
            return FALSE;
        } else if ((sampleContainsAlt || sampleGt.isMixed() || sampleGt.isNoCall()) &&
                (sampleOtherGtContainsAlt || sampleOtherGt.isMixed() || sampleOtherGt.isNoCall())) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private MatchEnum checkSamplePhased(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt, boolean isAffected) {
        Allele allele1 = sampleGt.getAllele(0);
        Allele allele2 = sampleGt.getAllele(1);
        Allele otherAllele1 = sampleOtherGt.getAllele(0);
        Allele otherAllele2 = sampleOtherGt.getAllele(1);
        //For phased data both variants can be present in an unaffected individual if both are on the same allele
        if ((isAlt(allele1) && isAlt(otherAllele2)) || isAlt(allele2) && isAlt(otherAllele1)) {
            return isAffected ? TRUE : FALSE;
        }
        if ((allele1.isReference() && otherAllele1.isReference()) || (allele2.isReference() && otherAllele2.isReference())) {
            return isAffected? FALSE : TRUE;
        }
        return POTENTIAL;
    }

    private MatchEnum checkAffectedSampleUnphased(EffectiveGenotype sampleGt, EffectiveGenotype sampleOtherGt) {
        if (sampleGt.hasAltAllele() && !sampleGt.isHom() && sampleOtherGt.hasAltAllele() && !sampleOtherGt.isHom()) {
            return TRUE;
        }
        boolean gtMissingOrMixed = sampleGt.isNoCall() || sampleGt.isMixed();
        boolean otherGtMissingOrMixed = sampleOtherGt.isNoCall() || sampleOtherGt.isMixed();
        boolean hasVariantAndOtherGtMissing = sampleGt.hasAltAllele() && !sampleGt.isHom() && otherGtMissingOrMixed;
        boolean hasOtherVariantAndGtMissing = gtMissingOrMixed && sampleOtherGt.hasAltAllele() && !sampleOtherGt.isHom();
        if (hasVariantAndOtherGtMissing
                || hasOtherVariantAndGtMissing
                || (gtMissingOrMixed && otherGtMissingOrMixed)) {
            return POTENTIAL;
        }
        return FALSE;
    }
}
