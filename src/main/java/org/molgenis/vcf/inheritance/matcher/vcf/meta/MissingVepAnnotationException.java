package org.molgenis.vcf.inheritance.matcher.vcf.meta;

import static java.lang.String.format;

import java.io.Serial;

public class MissingVepAnnotationException extends RuntimeException {
  @Serial private static final long serialVersionUID = 1L;

  public MissingVepAnnotationException(String field) {
    super(format("VEP annotation is missing field '%s'.", field));
  }
}
