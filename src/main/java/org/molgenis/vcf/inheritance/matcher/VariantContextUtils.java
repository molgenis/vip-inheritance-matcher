package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;

public class VariantContextUtils {

  private VariantContextUtils() {
  }

  public static boolean onAutosome(VcfRecord vcfRecord) {
    String contigId = vcfRecord.getContig();
    return contigId != null && ContigUtils.isAutosome(contigId);
  }

  public static boolean onChromosomeX(VcfRecord vcfRecord) {
    String contigId = vcfRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeX(contigId);
  }

  public static boolean onChromosomeY(VcfRecord vcfRecord) {
    String contigId = vcfRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeY(contigId);
  }

  public static boolean onChromosomeMt(VcfRecord vcfRecord) {
    String contigId = vcfRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeMt(contigId);
  }
}
