package org.molgenis.vcf.inheritance.matcher.model;
import lombok.Builder;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;

@Value
@Builder
public class CompoundCheckResult {
    VcfRecord possibleCompound;
    boolean isCertain;
}
