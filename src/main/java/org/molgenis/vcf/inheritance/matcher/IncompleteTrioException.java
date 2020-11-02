package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class IncompleteTrioException extends RuntimeException {

  public IncompleteTrioException(String proband) {
    super(format("Trio for sample '%s' is incomplete.", proband));
  }
}
