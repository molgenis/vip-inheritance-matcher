package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.springframework.lang.Nullable;

@Value
@Builder
public class InheritanceMode {

  @NonNull InheritanceModeEnum inheritanceModeEnum;
  @Nullable
  SubInheritanceMode subInheritanceMode;
}
