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
  Set<SubInheritanceMode> subInheritanceModes = new HashSet<>();
  @Builder.Default
  Set<String> compounds = new HashSet<>();
  @Builder.Default
  boolean denovo = false;

  public void addInheritanceMode(InheritanceMode inheritanceMode) {
    inheritanceModes.add(inheritanceMode);
  }

  public void addSubInheritanceMode(SubInheritanceMode subInheritanceMode) {
    subInheritanceModes.add(subInheritanceMode);
  }
}
