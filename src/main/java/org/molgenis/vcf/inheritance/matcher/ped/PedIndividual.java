package org.molgenis.vcf.inheritance.matcher.ped;

import lombok.NonNull;
import lombok.Value;

@Value
public class PedIndividual {
  public enum Sex {
    MALE,
    FEMALE,
    UNKNOWN
  }

  public enum AffectionStatus {
    AFFECTED,
    UNAFFECTED,
    UNKNOWN
  }

  @NonNull String familyId;

  @NonNull String id;

  @NonNull String paternalId;

  @NonNull String maternalId;

  @NonNull Sex sex;

  @NonNull PedIndividual.AffectionStatus affectionStatus;
}
