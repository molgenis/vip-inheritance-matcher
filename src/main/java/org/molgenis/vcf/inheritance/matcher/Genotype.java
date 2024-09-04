package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Genotype {
    htsjdk.variant.variantcontext.Genotype genotype;
    private final VariantContext variantContext;
    private final List<Allele> pathogenicAlleles;

    public Genotype(htsjdk.variant.variantcontext.Genotype genotype, VariantContext variantContext, List<Allele> pathogenicAlleles) {
        this.genotype = requireNonNull(genotype);
        this.variantContext = requireNonNull(variantContext);
        this.pathogenicAlleles = requireNonNull(pathogenicAlleles);
    }

    public htsjdk.variant.variantcontext.Genotype unwrap() {
        return genotype;
    }

    //Consider Benign allele as a REF allele
    //FIXME: private
    public htsjdk.variant.variantcontext.Genotype getEffectiveGenotype() {
        if (genotype.isNoCall()) {
            return genotype;
        }
        return GenotypeBuilder.create(genotype.getSampleName(), genotype.getAlleles().stream().map(this::mapAllele).toList());
    }

    private Allele mapAllele(Allele allele) {
        if(isBenign(allele)){
            return variantContext.getReference();
        }
        return allele;
    }

    private boolean isBenign(Allele allele) {
        return !this.pathogenicAlleles.contains(allele);
    }

    public boolean isHomRef() {
        //TODO:implement
        return genotype.isHomRef();
    }

    public boolean isNoCall() {
        //TODO:implement
        return genotype.isNoCall();
    }

    public boolean isMixed() {
        //TODO:implement
        return genotype.isMixed();
    }

    public int getPloidy() {
        return genotype.getPloidy();
    }

    public boolean isCalled() {
        return genotype.isCalled();
    }

    public boolean isHom() {
        return genotype.isHom();
    }

    public boolean hasAltAllele() {
        return genotype.hasAltAllele();
    }

    public List<Allele> getAlleles() {
        return genotype.getAlleles();
    }

    public boolean isHet() {
        return genotype.isHet();
    }

    public boolean isPhased() {
        return genotype.isPhased();
    }

    public boolean isHomAlt() {
        return genotype.isHomVar();
    }

    public Allele getAllele(int i) {
        return genotype.getAllele(i);
    }
}
