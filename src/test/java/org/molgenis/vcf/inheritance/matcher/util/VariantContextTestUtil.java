package org.molgenis.vcf.inheritance.matcher.util;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;

import java.util.*;

import static htsjdk.variant.variantcontext.Allele.*;
import static java.util.Collections.*;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.vcf.Genotype.PHASING_BLOCK;

public class VariantContextTestUtil {

    public static final Allele REF = Allele.REF_T;

    public static VcfRecord createVariantContext(List<Genotype> genotypes, String vepData) {
        return createVariantContext(genotypes, vepData, "1");
    }

    public static VcfRecord createVariantContext(List<Genotype> genotypes, String vepData, String contig) {
        VariantContextBuilder builder = new VariantContextBuilder();
        builder.chr(contig);
        builder.start(12345);
        builder.stop(12345);
        builder.alleles(Arrays.asList(REF, ALT_A, Allele.ALT_G, Allele.ALT_C, Allele.ALT_N));
        builder.genotypes(genotypes);
        builder.attribute("CSQ", vepData);

        return new VcfRecord(builder.make(), Set.of(ALT_A,ALT_G,ALT_N), Set.of(new GeneInfo("GENE1", "SOURCE", emptySet())));
    }

    public static Genotype createGenotype(String sample, String format) {
        boolean isPhased = false;
        String[] formatSplit = format.split(":");
        String[] gtSplit;
        String gt= formatSplit[0];
        if (gt.contains("|")) {
            isPhased = true;
            gtSplit = gt.split("\\|");
        } else if (gt.contains("/")) {
            gtSplit = gt.split("/");
        } else {
            gtSplit = new String[]{gt};
        }
        List<Allele> alleles = new ArrayList<>();
        alleles.add(getAllele(gtSplit, 0));
        if (gtSplit.length > 1) {
            alleles.add(getAllele(gtSplit, 1));
        }
        GenotypeBuilder genotypeBuilder = new GenotypeBuilder();
        genotypeBuilder.name(sample);
        genotypeBuilder.alleles(alleles);
        genotypeBuilder.phased(isPhased);
        if(formatSplit.length > 1){
            genotypeBuilder.attribute(PHASING_BLOCK, formatSplit[1]);
        }
        return genotypeBuilder.make();
    }

    private static Allele getAllele(String[] gtSplit, int i) {
        switch (gtSplit[i]) {
            case "0" -> {
                return REF;
            }
            case "1" -> {
                return ALT_A;
            }
            case "2" -> {
                return ALT_G;
            }
            case "3" -> {
                return ALT_C;
            }
            case "4" -> {
                return ALT_N;
            }
            default -> {
                return NO_CALL;
            }
        }
    }

    public static MatchEnum mapExpectedString(String expectedString) {
        return switch (expectedString) {
            case "true" -> TRUE;
            case "false" -> FALSE;
            case "possible" -> POTENTIAL;
            default -> throw new IllegalArgumentException("Value should be true, false or possible.");
        };
    }

}