package org.molgenis.vcf.inheritance.matcher.vcf.meta;

import java.io.Serial;

import static java.lang.String.format;

public class MissingInfoException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public MissingInfoException(String info) {
    super(format("Input is missing INFO field '%s'.", info));
  }
}
