package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.NonFinal;

@Data
@NonFinal
@Builder
public class Sample {

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

  boolean proband;
}
