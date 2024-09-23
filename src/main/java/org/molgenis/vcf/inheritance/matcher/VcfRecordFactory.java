package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;

import java.util.Set;

public interface VcfRecordFactory {
    VariantRecord create(VariantContext variantContext, Set<String> pathogenicClasses);
}
