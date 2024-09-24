package org.molgenis.vcf.inheritance.matcher.model;
import lombok.Builder;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;

@Value
@Builder
public class CompoundCheckResult {
    VariantRecord possibleCompound;
    boolean isCertain;
}
