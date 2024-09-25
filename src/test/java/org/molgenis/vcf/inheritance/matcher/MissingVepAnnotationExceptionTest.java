package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.MissingVepAnnotationException;

class MissingVepAnnotationExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "VEP annotation is missing field 'Gene'.",
        new MissingVepAnnotationException("Gene").getMessage());
  }
}