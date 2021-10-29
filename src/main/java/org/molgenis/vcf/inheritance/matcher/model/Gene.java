package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;
import lombok.Value;

@Value
public class Gene {

  String id;
  Set<InheritanceMode> inheritanceModes;
}
