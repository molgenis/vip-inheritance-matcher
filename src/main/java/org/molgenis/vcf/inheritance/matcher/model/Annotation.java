package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.NonFinal;

@Data
@Builder
@NonFinal
public class Annotation {

  @NonNull
  Inheritance inheritance;
  Set<String> matchingGenes;
}
