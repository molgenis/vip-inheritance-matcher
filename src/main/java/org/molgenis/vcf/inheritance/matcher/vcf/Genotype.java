package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.Allele;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Genotype {
    public static final String PHASING_BLOCK = "PS";
    final htsjdk.variant.variantcontext.Genotype originalGenotype;

    public Genotype(htsjdk.variant.variantcontext.Genotype originalGenotype) {
        this.originalGenotype = requireNonNull(originalGenotype);
    }

    public htsjdk.variant.variantcontext.Genotype unwrap() {
        return originalGenotype;
    }

    //combination of no_call/call or fully called gt with single VUS allele
    public boolean isMixed() {
        return originalGenotype.isMixed() || (!originalGenotype.isHom());
    }

    public int getPloidy() {
        return originalGenotype.getPloidy();
    }

    public boolean isCalled() {
        return originalGenotype.isCalled();
    }

    public List<Allele> getAlleles() {
        return originalGenotype.getAlleles();
    }

    public boolean isPhased() {
        return originalGenotype.isPhased();
    }

    public Allele getAllele(int i) {
        return originalGenotype.getAllele(i);
    }

    public boolean isHomRef() {
        return originalGenotype.isHomRef();
    }

    public boolean isNoCall() {
        return originalGenotype.isNoCall();
    }

    public boolean hasMissingAllele() {
        return originalGenotype.getAlleles().stream().anyMatch(Allele::isNoCall);
    }

    public boolean hasReference() {
        return originalGenotype.getAlleles().stream().anyMatch(Allele::isReference);
    }

    public boolean hasAltAllele() {
        return originalGenotype.hasAltAllele();
    }

    public boolean isHom() {
        return originalGenotype.isHom();
    }

    public boolean isHet() {
        return originalGenotype.isHet();
    }

    public String getPhasingBlock() {
        return originalGenotype.getExtendedAttribute(PHASING_BLOCK) != null
                ? originalGenotype.getExtendedAttribute(PHASING_BLOCK).toString(): null;
    }
}
