package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.*;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.*;

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
            Map<String, List<VariantContext>> geneVariantMap,
            VariantContext variantContext, Pedigree family, MatchEnum isAr) {
        if (onAutosome(variantContext) && isAr != TRUE) {
            List<CompoundCheckResult> compounds = new ArrayList<>();
            Map<String, Gene> genes = vepMapper.getGenes(variantContext).getGenes();
            for (Gene gene : genes.values()) {
                checkForGene(geneVariantMap, variantContext, family, compounds, gene);
            }
            return compounds;
        }
        return Collections.emptyList();
    }

    private void checkForGene(Map<String, List<VariantContext>> geneVariantMap,
                              VariantContext variantContext, Pedigree family, List<CompoundCheckResult> compounds,
                              Gene gene) {
        List<VariantContext> variantContexts = geneVariantMap.get(gene.getId());
        if (variantContexts != null) {
            for (VariantContext otherVariantContext : variantContexts) {
                if (!otherVariantContext.equals(variantContext)) {
                    MatchEnum isPossibleCompound = checkFamily(family, variantContext, otherVariantContext);
                    if (isPossibleCompound != FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherVariantContext).isCertain(isPossibleCompound != POTENTIAL).build();
                        compounds.add(result);
                    }
                }
            }
        }
    }

    private MatchEnum checkFamily(Pedigree family, VariantContext variantContext,
                                VariantContext otherVariantContext) {
        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantContext, otherVariantContext));
        }
        if (results.contains(FALSE)) {
            return FALSE;
        } else if (results.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private MatchEnum checkSample(Sample sample, VariantContext variantContext, VariantContext otherVariantContext) {
        //Affected individuals have to be het. for both variants
        //Healthy individuals can be het. for one of the variants but cannot have both variants
        Genotype missingGenotype = GenotypeBuilder.createMissing(sample.getPerson().getIndividualId(), 2);

        Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
        Genotype sampleOtherGt = otherVariantContext.getGenotype(sample.getPerson().getIndividualId());
        sampleGt = sampleGt != null ? sampleGt : missingGenotype;
        sampleOtherGt = sampleOtherGt != null ? sampleOtherGt : missingGenotype;
        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> checkAffectedSample(sampleGt, sampleOtherGt);
            case UNAFFECTED -> checkUnaffectedSample(sampleGt, sampleOtherGt);
            case MISSING -> POTENTIAL;
        };
    }

    private MatchEnum checkAffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
        if (sampleGt.isHomRef() || sampleOtherGt.isHomRef()) {
            return FALSE;
        } else if (isHomAlt(sampleGt)
                || isHomAlt(sampleOtherGt)) {
            return FALSE;
        } else if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkAffectedSamplePhased(sampleGt, sampleOtherGt);
        }
        return checkAffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private MatchEnum checkUnaffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
        if ((sampleGt.isNoCall() && (hasVariant(sampleOtherGt) || sampleOtherGt.isNoCall()))
                || sampleOtherGt.isNoCall() && (hasVariant(sampleGt))) {
            return POTENTIAL;
        } else if (isHomAlt(sampleGt) || isHomAlt(sampleOtherGt)) {
            return FALSE;
        } else if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkUnaffectedSamplePhased(sampleGt, sampleOtherGt);
        }
        return checkUnaffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private MatchEnum checkUnaffectedSampleUnphased(Genotype sampleGt, Genotype sampleOtherGt) {
        boolean sampleContainsAlt = hasVariant(sampleGt);
        boolean sampleOtherGtContainsAlt = hasVariant(sampleOtherGt);
        if (sampleContainsAlt && sampleOtherGtContainsAlt) {
            return FALSE;
        } else if ((sampleContainsAlt || sampleGt.isMixed() || sampleGt.isNoCall()) &&
                (sampleOtherGtContainsAlt || sampleOtherGt.isMixed() || sampleOtherGt.isNoCall())) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private MatchEnum checkUnaffectedSamplePhased(Genotype sampleGt, Genotype sampleOtherGt) {
        Allele allele1 = sampleGt.getAllele(0);
        Allele allele2 = sampleGt.getAllele(1);
        Allele otherAllele1 = sampleOtherGt.getAllele(0);
        Allele otherAllele2 = sampleOtherGt.getAllele(1);
        //For phased data both variants can be present in an unaffected individual if both are on the same allele
        if ((isAlt(allele1) && isAlt(otherAllele2)) || isAlt(allele2) && isAlt(otherAllele1)) {
            return FALSE;
        }
        if ((allele1.isReference() && otherAllele1.isReference()) || (allele2.isReference() && otherAllele2.isReference())) {
            return TRUE;
        }
        return POTENTIAL;
    }

    private MatchEnum checkAffectedSampleUnphased(Genotype sampleGt, Genotype sampleOtherGt) {
        if (hasVariant(sampleGt) && !sampleGt.isHom() && hasVariant(sampleOtherGt) && !sampleOtherGt.isHom()) {
            return TRUE;
        }
        boolean gtMissingOrMixed = sampleGt.isNoCall() || sampleGt.isMixed();
        boolean otherGtMissingOrMixed = sampleOtherGt.isNoCall() || sampleOtherGt.isMixed();
        boolean hasVariantAndOtherGtMissing = (hasVariant(sampleGt) && !sampleGt.isHom() && otherGtMissingOrMixed);
        boolean hasOtherVariantAndGtMissing = (gtMissingOrMixed && hasVariant(sampleOtherGt) && !sampleOtherGt.isHom());
        if (hasVariantAndOtherGtMissing
                || hasOtherVariantAndGtMissing
                || (gtMissingOrMixed && otherGtMissingOrMixed)) {
            return POTENTIAL;
        }
        return FALSE;
    }

    private MatchEnum checkAffectedSamplePhased(Genotype sampleGt, Genotype sampleOtherGt) {
        Allele allele1 = sampleGt.getAllele(0);
        Allele allele2 = sampleGt.getAllele(1);
        Allele otherAllele1 = sampleOtherGt.getAllele(0);
        Allele otherAllele2 = sampleOtherGt.getAllele(1);
        //For phased data both variants can be present in an unaffected individual if both are on the same allele
        if ((isAlt(allele1) && isAlt(otherAllele2)) || isAlt(allele2) && isAlt(otherAllele1)) {
            return TRUE;
        }
        if ((allele1.isReference() && otherAllele1.isReference()) || (allele2.isReference() && otherAllele2.isReference())) {
            return FALSE;
        }
        return POTENTIAL;
    }
}
