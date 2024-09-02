package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;

public class Genotype {
    htsjdk.variant.variantcontext.Genotype genotype;

    public Genotype(htsjdk.variant.variantcontext.Genotype genotype) {
        this.genotype = requireNonNull(genotype);
    }

    //TODO: implement functions to treat benign as REF en pathogenic as ALT
}
