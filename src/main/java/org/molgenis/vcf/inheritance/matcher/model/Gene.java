package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;
import lombok.Value;

@Value
public class Gene {

  String id;
  String symbolSource;
  boolean isIncompletePenetrance;
  Set<InheritanceMode> inheritanceModes;

  public boolean equalsGeneId(Gene other) {
    return this.getId().equals(other.getId()) && this.getSymbolSource()
        .equals(other.getSymbolSource());
  }
}
