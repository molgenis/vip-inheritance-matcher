package org.molgenis.vcf.inheritance.matcher.ped;

import static java.lang.String.format;

public class UnsupportedPedException extends RuntimeException {
  private static final String MESSAGE =
      "Phenotype value '%s' that is not an affection status (-9, 0, 1 or 2) is unsupported";
  private final String token;

  public UnsupportedPedException(String token) {
    this.token = token;
  }

  @Override
  public String getMessage() {
    return format(MESSAGE, token);
  }
}
