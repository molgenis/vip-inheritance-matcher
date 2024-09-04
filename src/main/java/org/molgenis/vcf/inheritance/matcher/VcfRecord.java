package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class VcfRecord {
    VariantContext variantContext;
    private final List<Allele> pathogenicAlleles;

    public VcfRecord(VariantContext variantContext, List<Allele> pathogenicAlleles) {
        this.variantContext = requireNonNull(variantContext);
        this.pathogenicAlleles = requireNonNull(pathogenicAlleles);
    }

    public Genotype getGenotype(String sampleId){
        htsjdk.variant.variantcontext.Genotype gt = variantContext.getGenotype(sampleId);
        if(gt == null){
            return null;
        }
        return new Genotype(gt, variantContext, pathogenicAlleles);
    }

    public List<String> getAttributeAsStringList(String vepFieldId) {
        return variantContext.getAttributeAsStringList(vepFieldId, "");
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
