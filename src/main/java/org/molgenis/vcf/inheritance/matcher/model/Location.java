package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Value;

@Value
public class Location {

  String chromosome;
  int position;
}
