package org.molgenis.vcf.inheritance.matcher.model;

import java.util.List;
import lombok.Value;

@Value
public class Genotype {

  boolean isPhased;
  List<Integer> alleles;
}
