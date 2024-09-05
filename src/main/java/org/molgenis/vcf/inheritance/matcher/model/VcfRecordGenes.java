package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
@Data
@Builder
public class VcfRecordGenes {
    Map<String, Gene> genes;
    //FIXME: is this used?
    @Builder.Default
    boolean containsVcWithoutGene = false;
}
