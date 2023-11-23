package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.*;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.*;

import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

public class ArCompoundChecker {

    private final VepMapper vepMapper;

    public ArCompoundChecker(VepMapper vepMapper) {
        this.vepMapper = vepMapper;
    }

    public List<CompoundCheckResult> check(
            Map<String, List<VariantContext>> geneVariantMap,
            VariantContext variantContext, Pedigree family) {
        if (onAutosome(variantContext)) {
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
                    Boolean isPossibleCompound = checkFamily(family, variantContext, otherVariantContext);
                    if (isPossibleCompound != Boolean.FALSE) {
                        CompoundCheckResult result = CompoundCheckResult.builder().possibleCompound(otherVariantContext).isCertain(isPossibleCompound != null).build();
                        compounds.add(result);
                    }
                }
            }
        }
    }

    private Boolean checkFamily(Pedigree family, VariantContext variantContext,
                                VariantContext otherVariantContext) {
        Set<Boolean> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantContext, otherVariantContext));
        }
        if (results.contains(false)) {
            return false;
        } else if (results.contains(null)) {
            return null;
        }
        return true;
    }

    private Boolean checkSample(Sample sample, VariantContext variantContext, VariantContext otherVariantContext) {
        //Affected individuals have to be het. for both variants
        //Healthy individuals can be het. for one of the variants but cannot have both variants
        Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
        Genotype sampleOtherGt = otherVariantContext.getGenotype(sample.getPerson().getIndividualId());

        return switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED -> checkAffectedSample(sampleGt, sampleOtherGt);
            case UNAFFECTED -> checkUnaffectedSample(sampleGt, sampleOtherGt);
            case MISSING -> null;
            default -> throw new IllegalArgumentException();
        };
    }

    private Boolean checkAffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
        if (sampleGt.isHomRef() || sampleOtherGt.isHomRef()) {
            return false;
        } else if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkAffectedSamplePhased(sampleGt, sampleOtherGt);
        }
        return checkAffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private Boolean checkUnaffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
        if (sampleGt == null || sampleOtherGt == null) {
            return null;
        } else if (isHomAlt(sampleGt) || isHomAlt(sampleOtherGt)) {
            return false;
        }
        if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
            return checkUnaffectedSamplePhased(sampleGt, sampleOtherGt);
        }
        return checkUnaffectedSampleUnphased(sampleGt, sampleOtherGt);
    }

    private Boolean checkUnaffectedSampleUnphased(Genotype sampleGt, Genotype sampleOtherGt) {
        boolean sampleContainsAlt = hasVariant(sampleGt);
        boolean sampleOtherGtContainsAlt = hasVariant(sampleOtherGt);
        if (sampleContainsAlt && sampleOtherGtContainsAlt) {
            return false;
        } else if ((sampleContainsAlt || sampleGt.isMixed() || sampleGt.isNoCall()) &&
                (sampleOtherGtContainsAlt || sampleOtherGt.isMixed() || sampleOtherGt.isNoCall())) {
            return null;
        }
        return true;
    }

    private Boolean checkUnaffectedSamplePhased(Genotype sampleGt, Genotype sampleOtherGt) {
        Allele allele1 = sampleGt.getAllele(0);
        Allele allele2 = sampleGt.getAllele(1);
        Allele otherAllele1 = sampleOtherGt.getAllele(0);
        Allele otherAllele2 = sampleOtherGt.getAllele(1);
        //For phased data both variants can be present in an unaffected individual if both are on the same allele
        if ((isAlt(allele1) && isAlt(otherAllele2)) || isAlt(allele2) && isAlt(otherAllele1)) {
            return false;
        }
        if ((allele1.isReference() && otherAllele2.isReference()) || (allele2.isReference() && otherAllele1.isReference())) {
            return true;
        }
        return null;
    }

    private Boolean checkAffectedSampleUnphased(Genotype sampleGt, Genotype sampleOtherGt) {
        if (hasVariant(sampleGt) && !sampleGt.isHom() && hasVariant(sampleOtherGt) && !sampleOtherGt.isHom()) {
            return true;
        }
        if ((hasVariant(sampleGt) && sampleOtherGt.isMixed())
                || (sampleGt.isMixed() && hasVariant(sampleOtherGt)
                || sampleGt.isMixed() && sampleOtherGt.isMixed())) {
            return null;
        }
        return false;
    }

    private Boolean checkAffectedSamplePhased(Genotype sampleGt, Genotype sampleOtherGt) {
        Allele allele1 = sampleGt.getAllele(0);
        Allele allele2 = sampleGt.getAllele(1);
        Allele otherAllele1 = sampleOtherGt.getAllele(0);
        Allele otherAllele2 = sampleOtherGt.getAllele(1);
        //For phased data both variants can be present in an unaffected individual if both are on the same allele
        if ((isAlt(allele1) && isAlt(otherAllele2)) || isAlt(allele2) && isAlt(otherAllele1)) {
            return true;
        }
        if ((allele1.isReference() && otherAllele2.isReference()) || (allele2.isReference() && otherAllele1.isReference())) {
            return false;
        }
        return null;
    }
}
