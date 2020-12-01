package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class CsvReaderException extends RuntimeException {

  public CsvReaderException(String pedfile, Exception e) {
    super(format("An exception occurred while reading the PED file '%s': %s.", pedfile, e.getMessage()), e);
  }
}
