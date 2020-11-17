package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class UnexpectedValueFormatException extends
    RuntimeException {

  public UnexpectedValueFormatException(String field) {
    super(format("Unexpected %s value format, expecting 'FAMILY_ID:values'.", field));
  }
}
