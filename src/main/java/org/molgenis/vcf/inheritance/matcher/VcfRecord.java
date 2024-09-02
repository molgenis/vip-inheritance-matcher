package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;

import static java.util.Objects.requireNonNull;

public class VcfRecord {
    VariantContext variantContext;

    public VcfRecord(VariantContext variantContext) {
        this.variantContext = requireNonNull(variantContext);
    }

    public Genotype getGenotype(String sampleId){
        return new Genotype(variantContext.getGenotype(sampleId));
    }
}
