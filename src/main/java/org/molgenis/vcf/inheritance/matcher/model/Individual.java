package org.molgenis.vcf.inheritance.matcher.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Individual {
  @NonNull
  String id;
}
