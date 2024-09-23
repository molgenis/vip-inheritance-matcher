package org.molgenis.vcf.inheritance.matcher.model;
import lombok.Builder;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;

@Value
@Builder
public class CompoundCheckResult {
    VariantGeneRecord possibleCompound;
    boolean isCertain;
}
