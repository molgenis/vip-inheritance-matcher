package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;

import java.util.List;
import java.util.Set;

public record VcfRecord(VariantContext variantContext, Set<Allele> pathogenicAlleles, Set<GeneInfo> geneInfos) {

    public Genotype getGenotype(String sampleId) {
        htsjdk.variant.variantcontext.Genotype gt = variantContext.getGenotype(sampleId);
        if (gt == null) {
            return null;
        }
        return new Genotype(gt);
    }

    public List<Allele> getAlternateAlleles() {
        return variantContext.getAlternateAlleles();
    }

    public String getContig() {
        return variantContext.getContig();
    }

    public int getStart() {
        return variantContext.getStart();
    }

    public Allele getReference() {
        return variantContext.getReference();
    }

    public int getAlleleIndex(Allele allele){
        return variantContext.getAlleleIndex(allele);
    }
}
