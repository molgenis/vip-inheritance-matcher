package org.molgenis.vcf.inheritance.matcher;

import lombok.NonNull;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

@Value
public class PedIndividual {

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
