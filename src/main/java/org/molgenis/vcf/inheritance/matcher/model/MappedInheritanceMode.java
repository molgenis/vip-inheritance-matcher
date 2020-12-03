package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class MappedInheritanceMode {

  @NonNull InheritanceMode inheritanceMode;
  @NonNull boolean isDenovo;
}
