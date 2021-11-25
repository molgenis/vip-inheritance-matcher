package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pedigree {

  String id;
  Map<String, Individual> members;
}
