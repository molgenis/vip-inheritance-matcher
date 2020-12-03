package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class UnexpectedInheritanceModeException extends
    RuntimeException {

  public UnexpectedInheritanceModeException(String mode) {
    super(format("Unexpected inheritance mode '%s'.", mode));
  }
}
