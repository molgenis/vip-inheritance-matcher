package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Sample {
  @NonNull
  Person person;

  // index of the sample in the VCF, -1 means the sample is not available in the file.
  @NonNull
  int index;
}
