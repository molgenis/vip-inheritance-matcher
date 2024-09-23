package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Getter;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class VariantGeneRecord {

    private final VariantContext variantContext;
    @Getter
    private final Set<Allele> pathogenicAlleles;
    @Getter
    private final GeneInfo geneInfo;

    public VariantGeneRecord(VariantContext variantContext, Set<Allele> pathogenicAlleles, GeneInfo geneInfo) {
        this.variantContext = requireNonNull(variantContext);
        this.pathogenicAlleles = requireNonNull(pathogenicAlleles);
        this.geneInfo = requireNonNull(geneInfo);
    }

    public EffectiveGenotype getGenotype(String sampleId) {
        htsjdk.variant.variantcontext.Genotype gt = variantContext.getGenotype(sampleId);
        if (gt == null) {
            return null;
        }
        if(pathogenicAlleles.isEmpty()){
            pathogenicAlleles.addAll(variantContext.getAlternateAlleles());
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
