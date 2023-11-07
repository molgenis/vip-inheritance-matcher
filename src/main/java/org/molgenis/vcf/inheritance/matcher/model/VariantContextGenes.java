package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
@Data
@Builder
public class VariantContextGenes{
    Map<String, Gene> genes;
    @Builder.Default
    boolean containsVcWithoutGene = false;
}
