package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class EffectiveGenotype {
    final htsjdk.variant.variantcontext.Genotype genotype;

    public EffectiveGenotype(htsjdk.variant.variantcontext.Genotype originalGenotype) {
        this.genotype = requireNonNull(originalGenotype);
    }

    public htsjdk.variant.variantcontext.Genotype unwrap() {
        return genotype;
    }

    //combination of no_call/call or fully called gt with single VUS allele
    public boolean isMixed() {
        return genotype.isMixed() || (!genotype.isHom());
    }

    public int getPloidy() {
        return genotype.getPloidy();
    }

    public boolean isCalled() {
        return genotype.isCalled();
    }

    public List<Allele> getAlleles() {
        return genotype.getAlleles();
    }

    public boolean isPhased() {
        return genotype.isPhased();
    }

    public Allele getAllele(int i) {
        return genotype.getAllele(i);
    }

    public boolean isHomRef() {
        return genotype.isHomRef();
    }

    public boolean isNoCall() {
        return genotype.isNoCall();
    }

    public boolean hasMissingAllele() {
        return genotype.getAlleles().stream().anyMatch(Allele::isNoCall);
    }

    public boolean hasReference() {
        return genotype.getAlleles().stream().anyMatch(Allele::isReference);
    }

    public boolean hasAltAllele() {
        return genotype.hasAltAllele();
    }

    public boolean isHom() {
        return genotype.isHom();
    }

    public boolean isHet() {
        return genotype.isHet();
    }
}
