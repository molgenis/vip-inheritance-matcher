package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Optional;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class VariantContextUtils {

  private VariantContextUtils() {
  }

  public static boolean onAutosome(VariantContext variantContext) {
    String contigId = variantContext.getContig();
    return contigId != null && ContigUtils.isAutosome(contigId);
  }

  public static boolean onChromosomeX(VariantContext variantContext) {
    String contigId = variantContext.getContig();
    return contigId != null && ContigUtils.isChromosomeX(contigId);
  }

  public static boolean onChromosomeY(VariantContext variantContext) {
    String contigId = variantContext.getContig();
    return contigId != null && ContigUtils.isChromosomeY(contigId);
  }

  public static boolean onChromosomeMt(VariantContext variantContext) {
    String contigId = variantContext.getContig();
    return contigId != null && ContigUtils.isChromosomeMt(contigId);
  }

  public static Optional<Genotype> getGenotype(VariantContext variantContext,
      Sample sample) {
    String individualId = sample.getIndividualId();
    return Optional.ofNullable(variantContext.getGenotype(individualId));
  }
}
