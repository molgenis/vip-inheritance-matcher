package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.Set;

public interface VcfRecordFactory {
    VcfRecord create(VariantContext variantContext, Set<String> pathogenicClasses);
}
