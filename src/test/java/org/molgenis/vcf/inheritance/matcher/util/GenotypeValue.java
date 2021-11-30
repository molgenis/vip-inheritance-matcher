package org.molgenis.vcf.inheritance.matcher.util;

import htsjdk.variant.variantcontext.Allele;
import java.util.List;
import lombok.Value;

@Value
public class GenotypeValue {
  String sample;
  List<Allele> alleles;
  boolean phased;
}
