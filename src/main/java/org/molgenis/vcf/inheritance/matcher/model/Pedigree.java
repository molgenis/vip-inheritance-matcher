package org.molgenis.vcf.inheritance.matcher.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Pedigree {
  String id;
  List<Individual> members;
}
