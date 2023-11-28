package org.molgenis.vcf.inheritance.matcher.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Inheritance {
  @Builder.Default
  Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = new HashSet<>();
  @Builder.Default
  Set<String> compounds = new HashSet<>();

  @Builder.Default
  MatchEnum match = MatchEnum.POTENTIAL;

  @Builder.Default
  MatchEnum denovo = MatchEnum.POTENTIAL;

  public void addInheritanceMode(PedigreeInheritanceMatch pedigreeInheritanceMatch) {
    pedigreeInheritanceMatches.add(pedigreeInheritanceMatch);
  }
}
