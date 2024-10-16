package org.molgenis.vcf.inheritance.matcher.model;

import java.util.*;

import lombok.Builder;
import lombok.Data;
import org.molgenis.vcf.utils.sample.model.Sample;

@Data
@Builder
public class InheritanceResult {

  @Builder.Default
  Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = new HashSet<>();
  @Builder.Default
  Map<GeneInfo,Set<CompoundCheckResult>> compounds = new HashMap<>();

  public void addInheritanceMode(PedigreeInheritanceMatch pedigreeInheritanceMatch) {
    pedigreeInheritanceMatches.add(pedigreeInheritanceMatch);
  }

  Map<Sample, MatchEnum> denovo;
}
