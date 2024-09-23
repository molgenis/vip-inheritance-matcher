package org.molgenis.vcf.inheritance.matcher;

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
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "Possible Compound hetrozygote variants."));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(DENOVO, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.Integer, "De novo variant."));
        vcfHeader.addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.Integer, "Inheritance Match: Genotypes, affected statuses and known gene inheritance patterns match."));
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

    VariantContext annotateInheritance(VariantRecord variantRecord, Collection<Pedigree> pedigrees, Map<Pedigree, InheritanceResult> inheritanceResultMap, List<String> probands) {
        GenotypesContext genotypesContext = GenotypesContext.copy(variantRecord.variantContext().getGenotypes());
        VariantContextBuilder variantContextBuilder = new VariantContextBuilder(variantRecord.variantContext());
        for (Pedigree pedigree : pedigrees) {
            pedigree.getMembers().values().stream().filter(sample -> probands.isEmpty() || probands.contains(sample.getPerson().getIndividualId())).forEach(sample -> {
                Genotype genotype = variantRecord.getGenotype(sample.getPerson().getIndividualId());
                if (inheritanceResultMap.containsKey(pedigree) && genotype != null) {
                    genotypesContext.replace(annotateGenotype(inheritanceResultMap.get(pedigree), genotype, sample));
                }
            });
        }
        return variantContextBuilder.genotypes(genotypesContext).make();
    }

    private Genotype annotateGenotype(InheritanceResult inheritanceResult, Genotype genotype, Sample sample) {
        GenotypeBuilder genotypeBuilder = new GenotypeBuilder(genotype);
        List<String> VIM = new ArrayList<>();
        List<String> VIC = new ArrayList<>();
        List<String> VIG = new ArrayList<>();
        List<String> VI = new ArrayList<>();
        for (InheritanceGeneResult inheritanceGeneResult : inheritanceResult.getInheritanceGeneResults()) {
            String compounds = inheritanceGeneResult.getCompounds().isEmpty() ? "" : String.join("&", inheritanceGeneResult.getCompounds().stream().map(this::createKey).toList());
            MatchEnum match = inheritanceGeneResult.getMatch();
            VI.add(String.join("&", mapInheritanceModes(inheritanceGeneResult)));
            VIM.add(mapInheritanceMatch(match));
            VIC.add(compounds);
            if ((match == TRUE || match == POTENTIAL)) {
                VIG.add(inheritanceGeneResult.getGeneInfo().geneId());
            }
        }
        genotypeBuilder.attribute(INHERITANCE_MODES, String.join(",", VI));
        genotypeBuilder.attribute(INHERITANCE_MATCH, String.join(",", VIM));
        genotypeBuilder.attribute(POSSIBLE_COMPOUND, String.join(",", VIC));
        genotypeBuilder.attribute(MATCHING_GENES, String.join(",", VIG));
        genotypeBuilder.attribute(DENOVO, mapDenovoValue(inheritanceResult, sample));
        return genotypeBuilder.make();
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

    private Set<String> mapInheritanceModes(InheritanceGeneResult inheritanceResult) {
        Set<String> result = new HashSet<>();
        for (PedigreeInheritanceMatch pedigreeInheritanceMatch : inheritanceResult.getPedigreeInheritanceMatches()) {
            result.add(pedigreeInheritanceMatch.inheritanceMode().name());
        }
        return result;
    }

    private String createKey(CompoundCheckResult result) {
        VariantGeneRecord record = result.getPossibleCompound();
        return String.format("%s_%s_%s_%s", record.getContig(), record.getStart(), record.getReference().getBaseString(), record.getAlternateAlleles().stream().map(Allele::getBaseString).collect(Collectors.joining("/")));
    }
}
