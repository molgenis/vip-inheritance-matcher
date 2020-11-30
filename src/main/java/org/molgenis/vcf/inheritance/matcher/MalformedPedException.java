package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class MalformedPedException extends RuntimeException {

  public MalformedPedException(String fileName) {
    super(format("PED file '%s' is malformed, expecting 6 columns on every row.", fileName));
  }
}
