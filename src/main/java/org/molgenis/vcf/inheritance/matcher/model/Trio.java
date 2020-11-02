package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Trio {
  @NonNull String family;
  @NonNull Sample proband;
  @NonNull Sample father;
  @NonNull Sample mother;
}
