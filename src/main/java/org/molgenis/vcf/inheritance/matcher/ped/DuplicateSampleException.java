package org.molgenis.vcf.inheritance.matcher.ped;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class DuplicateSampleException extends RuntimeException {
  public DuplicateSampleException(String individualId) {
    super(format("Sample with id '%s' is present in multiple pedigree files.", individualId));
  }
}
