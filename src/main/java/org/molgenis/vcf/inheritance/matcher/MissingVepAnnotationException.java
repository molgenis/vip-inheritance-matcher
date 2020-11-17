package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

public class MissingVepAnnotationException extends
    RuntimeException {

  public MissingVepAnnotationException(String field) {
      super(format("VEP annotation is missing field '%s'.", field));
    }
}
