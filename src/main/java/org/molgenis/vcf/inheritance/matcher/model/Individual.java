package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Value;
import org.molgenis.vcf.inheritance.matcher.PedIndividual.AffectionStatus;
import org.springframework.lang.Nullable;

@Value
@Builder
public class Individual {

  String id;
  Sex sex;
  AffectionStatus affectionStatus;
  @Nullable
  Individual mother;
  @Nullable
  Individual father;
}
