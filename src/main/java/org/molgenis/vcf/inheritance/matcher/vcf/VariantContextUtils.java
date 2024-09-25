package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.ContigUtils;
import org.molgenis.vcf.utils.sample.model.Pedigree;

import java.util.HashSet;
import java.util.Set;

public class VariantContextUtils {

  private VariantContextUtils() {
  }

  public static boolean onAutosome(VcfRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isAutosome(contigId);
  }

  public static boolean onChromosomeX(VcfRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeX(contigId);
  }

  public static boolean onChromosomeMt(VcfRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeMt(contigId);
  }

  public static boolean onChromosomeY(VcfRecord variantGeneRecord) {
    String contigId = variantGeneRecord.getContig();
    return contigId != null && ContigUtils.isChromosomeY(contigId);
  }

  public static Set<Allele> getAltAlleles(VcfRecord variantGeneRecord, Pedigree pedigree){
    Set<Allele> altAlleles = new HashSet<>();
    for(String sample : pedigree.getMembers().keySet()){
      Genotype genotype = variantGeneRecord.getGenotype(sample);
      if(genotype != null) {
        genotype.unwrap().getAlleles().stream().filter(Allele::isNonReference).forEach(altAlleles::add);
      }
    }
    return altAlleles;
  }

}
