package org.molgenis.vcf.inheritance.matcher.model;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompoundCheckResult {
    VariantContext possibleCompound;
    boolean isCertain;
}
