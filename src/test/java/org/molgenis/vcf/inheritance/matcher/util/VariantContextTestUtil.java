package org.molgenis.vcf.inheritance.matcher.util;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariantContextTestUtil {

  public static final Allele REF = Allele.REF_T;
  public static final Allele ALT = Allele.ALT_A;

  public static VariantContext createVariantContext(List<Genotype> genotypes, String vep) {
    return createVariantContext(genotypes, vep, "1");
  }

  public static VariantContext createVariantContext(List<Genotype> genotypes, String vep,
      String contig) {
    VariantContextBuilder builder = new VariantContextBuilder();
    builder.chr(contig);
    builder.start(12345);
    builder.stop(12345);
    builder.alleles(Arrays.asList(REF, ALT));
    builder.genotypes(genotypes);
    builder.attribute("CSQ", vep);
    return builder.make();
  }

  public static Genotype createGenotype(String sample, String gt) {
    boolean isPhased = false;
    String[] gtSplit = null;
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
    return genotypeBuilder.make();
  }

  private static Allele getAllele(String[] gtSplit, int i) {
    if (gtSplit[i].equals("0")) {
      return REF;
    } else if (gtSplit[i].equals(".")) {
      return Allele.NO_CALL;
    } else {
      return ALT;
    }
  }

}