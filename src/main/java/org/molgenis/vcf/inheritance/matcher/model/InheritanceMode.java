package org.molgenis.vcf.inheritance.matcher.model;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class InheritanceMode {
  @NonNull InheritanceModeEnum mode;
  @Nullable
  Boolean isCompound;
}
