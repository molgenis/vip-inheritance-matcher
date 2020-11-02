package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InheritanceResults {
  Trio trio;
  String gene;
  Map<String, Set<InheritanceMode>> variantInheritanceResults;
}
