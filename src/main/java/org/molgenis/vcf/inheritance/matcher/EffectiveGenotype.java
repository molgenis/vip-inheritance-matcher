package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class EffectiveGenotype {
    htsjdk.variant.variantcontext.Genotype originalGenotype;
    private final VariantContext variantContext;
    private final List<Allele> pathogenicAlleles;
    private Genotype effectiveGenotype;

    public EffectiveGenotype(htsjdk.variant.variantcontext.Genotype originalGenotype, VariantContext variantContext, List<Allele> pathogenicAlleles) {
        this.originalGenotype = requireNonNull(originalGenotype);
        this.variantContext = requireNonNull(variantContext);
        this.pathogenicAlleles = requireNonNull(pathogenicAlleles);

        init();
    }

    public htsjdk.variant.variantcontext.Genotype unwrap() {
        return originalGenotype;
    }

    //Consider Benign allele as a REF allele
    private void init() {
        if (originalGenotype.isNoCall()) {
            effectiveGenotype = originalGenotype;
        }
        GenotypeBuilder genotypeBuilder = new GenotypeBuilder(originalGenotype);
        genotypeBuilder.alleles(originalGenotype.getAlleles().stream().map(this::mapAllele).toList());
        effectiveGenotype = genotypeBuilder.make();
    }

    private Allele mapAllele(Allele allele) {
        if (isPathogenic(allele) || allele.isNoCall()) {
            return allele;
        }
        return variantContext.getReference();
    }

    private boolean isPathogenic(Allele allele) {
        //if no pathogenic classes are provided consider all ALTs pathogenic
        if (allele.isNonReference() && pathogenicAlleles.isEmpty()) {
            return true;
        }
        return pathogenicAlleles.contains(allele);
    }

    public boolean isHomRef() {
        return effectiveGenotype.isHomRef();
    }

    public boolean isNoCall() {
        return effectiveGenotype.isNoCall();
    }

    public boolean isMixed() {
        return effectiveGenotype.isMixed();
    }

    public int getPloidy() {
        return effectiveGenotype.getPloidy();
    }

    public boolean isCalled() {
        return effectiveGenotype.isCalled();
    }

    public boolean isHom() {
        //for inheritance matching consider any ALT/ALT combination HOM_ALT
        return effectiveGenotype.isHom() || this.isHomAlt();
    }

    public boolean hasAltAllele() {
        return effectiveGenotype.hasAltAllele();
    }

    public List<Allele> getAlleles() {
        return effectiveGenotype.getAlleles();
    }

    public boolean isHet() {
        //for inheritance matching consider any ALT/ALT combination HOM_ALT
        return effectiveGenotype.isHet() && effectiveGenotype.hasRefAllele();
    }

    public boolean isPhased() {
        return effectiveGenotype.isPhased();
    }

    public boolean isHomAlt() {
        //for inheritance matching consider any ALT/ALT combination HOM_ALT
        return effectiveGenotype.isCalled() && !effectiveGenotype.isMixed() && !effectiveGenotype.hasRefAllele();
    }

    public Allele getAllele(int i) {
        return effectiveGenotype.getAllele(i);
    }
}
