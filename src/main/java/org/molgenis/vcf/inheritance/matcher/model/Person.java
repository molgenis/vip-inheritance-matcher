package org.molgenis.vcf.inheritance.matcher.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Person {
  @NonNull
  String familyId;

  @NonNull
  String individualId;

  @NonNull
  String paternalId;

  @NonNull
  String maternalId;

  @NonNull
  Sex sex;

  @NonNull
  AffectedStatus affectedStatus;
}
