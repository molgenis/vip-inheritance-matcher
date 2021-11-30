package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.NonFinal;

@Data
@NonFinal
@Builder
public class Individual {

  @NonNull
  String familyId;

  @NonNull
  String id;

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
