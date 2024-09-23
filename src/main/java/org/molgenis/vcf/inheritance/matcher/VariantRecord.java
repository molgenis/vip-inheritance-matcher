package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.NonNull;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResult;

import java.util.Map;

public record VariantRecord(Map<GeneInfo, VariantGeneRecord> variantGeneRecords, VariantContext variantContext,
                            InheritanceResult inheritanceResult) {
    public Genotype getGenotype(@NonNull String individualId) {
        return variantContext.getGenotype(individualId);
    }

    public String getContig() {
        return variantContext.getContig();
    }
}
