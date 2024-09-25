package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;

import java.util.List;
import java.util.Set;

public record VariantRecord(VariantContext variantContext, Set<Allele> pathogenicAlleles, Set<GeneInfo> geneInfos) {

    public EffectiveGenotype getGenotype(String sampleId) {
        htsjdk.variant.variantcontext.Genotype gt = variantContext.getGenotype(sampleId);
        if (gt == null) {
            return null;
        }
        return new EffectiveGenotype(gt);
    }

    public List<Allele> getAlternateAlleles() {
        return variantContext.getAlternateAlleles();
    }

    public VariantContext unwrap() {
        return variantContext;
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
}
