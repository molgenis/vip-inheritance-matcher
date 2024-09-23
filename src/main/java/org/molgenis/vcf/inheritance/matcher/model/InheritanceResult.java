package org.molgenis.vcf.inheritance.matcher.model;

import java.util.*;

import lombok.Builder;
import lombok.Data;
import org.molgenis.vcf.utils.sample.model.Sample;

@Data
@Builder
public class InheritanceResult {

  @Builder.Default
  Collection<InheritanceGeneResult> inheritanceGeneResults = new HashSet<>();
  Map<Sample, MatchEnum> denovo;
}
