package org.molgenis.vcf.inheritance.matcher.model;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.NonFinal;
import org.springframework.lang.Nullable;

@Data
@Builder
@NonFinal
public class Annotation {

  @NonNull String familyID;
  @Nullable
  Set<InheritanceMode> inheritanceMode;
  @NonNull boolean denovo;
  @Nullable
  List<String> matchingGenes;
}
