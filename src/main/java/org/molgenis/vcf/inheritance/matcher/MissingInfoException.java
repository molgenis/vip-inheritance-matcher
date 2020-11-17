package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class MissingInfoException extends RuntimeException {

  public MissingInfoException(String info) {
    super(format("Input is missing INFO field '%s'.", info));
  }
}
