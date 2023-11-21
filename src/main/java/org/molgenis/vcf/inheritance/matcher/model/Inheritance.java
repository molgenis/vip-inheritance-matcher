package org.molgenis.vcf.inheritance.matcher.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Inheritance {
  @Builder.Default
  Set<InheritanceMode> inheritanceModes = new HashSet<>();
  @Builder.Default
  Set<String> compounds = new HashSet<>();

  @Builder.Default
  InheritanceMatch match = InheritanceMatch.POTENTIAL;

  @Builder.Default
  Boolean denovo = null;

  @Builder.Default
  boolean isFamilyWithMissingGT = false;

  public void addInheritanceMode(InheritanceMode inheritanceMode) {
    inheritanceModes.add(inheritanceMode);
  }
}
