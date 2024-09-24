package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.utils.sample.model.Pedigree;

import java.util.HashSet;
import java.util.Set;

public class VariantContextUtils {

  private VariantContextUtils() {
  }

  public static boolean onAutosome(VariantRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isAutosome(contigId);
  }

  public static boolean onChromosomeX(VariantRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeX(contigId);
  }

  public static boolean onChromosomeMt(VariantRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeMt(contigId);
  }

  public static boolean onChromosomeY(VariantRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeY(contigId);
  }

  public static Set<Allele> getAltAlleles(VariantRecord variantGeneRecord, Pedigree pedigree){
    Set<Allele> altAlleles = new HashSet<>();
    for(String sample : pedigree.getMembers().keySet()){
      EffectiveGenotype effectiveGenotype = variantGeneRecord.getGenotype(sample);
      if(effectiveGenotype != null) {
        effectiveGenotype.unwrap().getAlleles().stream().filter(Allele::isNonReference).forEach(altAlleles::add);
      }
    }
    return altAlleles;
  }

}
