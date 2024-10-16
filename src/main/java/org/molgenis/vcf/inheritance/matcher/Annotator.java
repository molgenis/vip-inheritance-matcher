package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfFilterHeaderLines;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfFormatHeaderLines;
import static org.molgenis.vcf.utils.utils.HeaderUtils.fixVcfInfoHeaderLines;

import htsjdk.variant.variantcontext.*;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;

import java.util.*;
import java.util.stream.Collectors;

import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.utils.UnexpectedEnumException;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class Annotator {

    public static final String INHERITANCE_MODES = "VI";
    public static final String POSSIBLE_COMPOUND = "VIC";
    public static final String DENOVO = "VID";
    public static final String INHERITANCE_MATCH = "VIM";
    public static final String MATCHING_GENES = "VIG";

    VCFHeader annotateHeader(VCFHeader vcfHeader) {
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "An enumeration of possible inheritance modes based on the pedigree of the sample. Potential values: AD, AD_IP, AR, AR_C, XLR, XLD"));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1, VCFHeaderLineType.String, "Possible Compound hetrozygote variants."));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1, VCFHeaderLineType.Integer, "De novo variant."));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1, VCFHeaderLineType.Integer, "Inheritance Match: Genotypes, affected statuses and known gene inheritance patterns match."));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Genes with a (potential) inheritance match."));
        Set<VCFHeaderLine> headerLines = new LinkedHashSet<>();
        //workaround for "Escaped doublequotes in INFO descriptions result in invalid VCF file"
        // https://github.com/samtools/htsjdk/issues/1661
        headerLines.addAll(fixVcfInfoHeaderLines(vcfHeader));
        headerLines.addAll(fixVcfFormatHeaderLines(vcfHeader));
        headerLines.addAll(fixVcfFilterHeaderLines(vcfHeader));
        headerLines.addAll(vcfHeader.getOtherHeaderLines());
        headerLines.addAll(vcfHeader.getContigLines());

        vcfHeader = new VCFHeader(headerLines, vcfHeader.getGenotypeSamples());
        return vcfHeader;
    }

    VariantContext annotateInheritance(VcfRecord vcfRecord, Collection<Pedigree> pedigrees, Map<Pedigree, InheritanceResult> inheritanceResultMap, List<String> probands) {
        GenotypesContext genotypesContext = GenotypesContext.copy(vcfRecord.variantContext().getGenotypes());
        VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vcfRecord.variantContext());
        for (Pedigree pedigree : pedigrees) {
            pedigree.getMembers().values().stream().filter(sample -> probands.isEmpty() || probands.contains(sample.getPerson().getIndividualId())).forEach(sample -> {
                Genotype effectiveGenotype = vcfRecord.getGenotype(sample.getPerson().getIndividualId());
                htsjdk.variant.variantcontext.Genotype genotype = effectiveGenotype != null ? effectiveGenotype.unwrap() : null;
                if (inheritanceResultMap.containsKey(pedigree) && genotype != null) {
                    genotypesContext.replace(annotateGenotype(inheritanceResultMap.get(pedigree), genotype, sample, vcfRecord));
                }
            });
        }
        return variantContextBuilder.genotypes(genotypesContext).make();
    }

    private htsjdk.variant.variantcontext.Genotype annotateGenotype(InheritanceResult inheritanceResult, htsjdk.variant.variantcontext.Genotype genotype, Sample sample, VcfRecord vcfRecord) {
        GenotypeBuilder genotypeBuilder = new GenotypeBuilder(genotype);
        Set<String> vig = new HashSet<>();
        Set<String> compounds = getCompoundStrings(inheritanceResult.getCompounds());
        String vic = inheritanceResult.getCompounds().isEmpty() ? "" : String.join(",", compounds);
        MatchEnum match = getMatch(inheritanceResult, vcfRecord);
        Set<String> vi = mapInheritanceModes(inheritanceResult);
        String vim = mapInheritanceMatch(match);
        if ((match == TRUE || match == POTENTIAL)) {
            vig = getMatchingGenes(inheritanceResult.getPedigreeInheritanceMatches(), vcfRecord.geneInfos(), inheritanceResult.getCompounds());
        }

        genotypeBuilder.attribute(INHERITANCE_MODES, String.join(",", vi));
        genotypeBuilder.attribute(INHERITANCE_MATCH, vim);
        genotypeBuilder.attribute(POSSIBLE_COMPOUND, vic);
        genotypeBuilder.attribute(MATCHING_GENES, vig.stream().sorted().collect(Collectors.joining(",")));
        genotypeBuilder.attribute(DENOVO, mapDenovoValue(inheritanceResult, sample));
        return genotypeBuilder.make();
    }

    private Set<String> getCompoundStrings(Map<GeneInfo, Set<CompoundCheckResult>> compounds) {
        Set<String> result = new HashSet<>();
        compounds.values().forEach(compoundsForGene -> compoundsForGene.forEach(compound -> result.add(createKey(compound))));
        return result;
    }

    private Set<String> getMatchingGenes(Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches, Set<GeneInfo> geneInfos, Map<GeneInfo, Set<CompoundCheckResult>> compounds) {
        Set<String> results = new HashSet<>();
        for (PedigreeInheritanceMatch pedigreeInheritanceMatch : pedigreeInheritanceMatches) {
            for (GeneInfo geneInfo : geneInfos) {
                if (geneInfo.inheritanceModes().stream().anyMatch(geneMode -> isMatch(geneMode,
                        pedigreeInheritanceMatch.inheritanceMode())) && pedigreeInheritanceMatch.inheritanceMode() != AR_C
                        || compounds.get(geneInfo) != null && !compounds.get(geneInfo).isEmpty()) {
                    results.add(geneInfo.geneId());
                }
            }
        }
        return results;
    }

    private static String mapDenovoValue(InheritanceResult inheritanceResult, Sample sample) {
        return switch (inheritanceResult.getDenovo().get(sample)) {
            case TRUE -> "1";
            case FALSE -> "0";
            case POTENTIAL -> "";
        };
    }

    private static String mapInheritanceMatch(MatchEnum match) {
        String inheritanceMatch;
        switch (match) {
            case TRUE -> inheritanceMatch = "1";
            case FALSE -> inheritanceMatch = "0";
            case POTENTIAL -> inheritanceMatch = "";
            default -> throw new UnexpectedEnumException(match);
        }
        return inheritanceMatch;
    }

    private Set<String> mapInheritanceModes(InheritanceResult inheritanceResult) {
        Set<String> result = new HashSet<>();
        for (PedigreeInheritanceMatch pedigreeInheritanceMatch : inheritanceResult.getPedigreeInheritanceMatches()) {
            result.add(pedigreeInheritanceMatch.inheritanceMode().name());
        }
        return result;
    }

    private String createKey(CompoundCheckResult result) {
        VcfRecord vcfRecord = result.getPossibleCompound();
        return String.format("%s_%s_%s_%s", vcfRecord.getContig(), vcfRecord.getStart(), vcfRecord.getReference().getBaseString(), vcfRecord.getAlternateAlleles().stream().map(Allele::getBaseString).collect(Collectors.joining("/")));
    }

    /**
     * If there is a match between sample inheritance modes and gene inheritance modes:
     * - inheritance match is true
     * If there are no matches between sample inheritance modes and gene inheritance modes:
     * - inheritance match is unknown ifa gene has unknown inheritance pattern.
     * - inheritance match is false if a gene has known (but mismatching) inheritance pattern.
     */
    public MatchEnum getMatch(InheritanceResult inheritanceResult, VcfRecord vcfRecord) {
        //If no inheritance pattern is suitable for the sample, regardless of the gene: inheritance match is false.
        Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = inheritanceResult.getPedigreeInheritanceMatches();
        if (pedigreeInheritanceMatches.isEmpty()) {
            return FALSE;
        }
        boolean containsUnknownGene = vcfRecord.geneInfos().isEmpty() || vcfRecord.geneInfos().stream().anyMatch(geneInfo -> geneInfo.inheritanceModes().isEmpty());

        Set<MatchEnum> result = new HashSet<>();
        for (GeneInfo geneInfo : vcfRecord.geneInfos()) {
            if (geneInfo.inheritanceModes().stream()
                    .anyMatch(geneInheritanceMode -> isMatch(pedigreeInheritanceMatches, geneInheritanceMode))) {
                if (pedigreeInheritanceMatches.stream().anyMatch(pedigreeInheritanceMatch -> !pedigreeInheritanceMatch.isUncertain())) {
                    result.add(TRUE);
                } else {
                    result.add(POTENTIAL);
                }
            }
        }
        if (result.contains(TRUE)) {
            return TRUE;
        }
        if (result.contains(POTENTIAL) || containsUnknownGene) {
            return POTENTIAL;
        }
        return FALSE;
    }

    private static Boolean isMatch(Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches, InheritanceMode geneInheritanceMode) {
        for (PedigreeInheritanceMatch pedigreeInheritanceMatch : pedigreeInheritanceMatches) {
            InheritanceMode pedigreeInheritanceMode = pedigreeInheritanceMatch.inheritanceMode();
            if(isMatch(geneInheritanceMode, pedigreeInheritanceMode)){
                return true;
            }
        }
        return false;
    }

    private static boolean isMatch(InheritanceMode geneInheritanceMode, InheritanceMode pedigreeInheritanceMode) {
        switch (pedigreeInheritanceMode) {
            case AD, AD_IP -> {
                if (geneInheritanceMode == AD) {
                    return true;
                }
            }
            case AR, AR_C -> {
                if (geneInheritanceMode == AR) {
                    return true;
                }
            }
            case XLR, XLD -> {
                if (geneInheritanceMode == XL) {
                    return true;
                }
            }
            case YL -> {
                if (geneInheritanceMode == YL) {
                    return true;
                }
            }
            case MT -> {
                if (geneInheritanceMode == MT) {
                    return true;
                }
            }
            default -> throw new UnexpectedEnumException(pedigreeInheritanceMode);
        }
        return false;
    }

}
