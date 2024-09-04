package org.molgenis.vcf.inheritance.matcher.model;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Builder;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;

@Value
@Builder
public class CompoundCheckResult {
    VcfRecord possibleCompound;
    boolean isCertain;
}
