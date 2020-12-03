package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MissingVepAnnotationExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "VEP annotation is missing field 'Gene'.",
        new MissingVepAnnotationException("Gene").getMessage());
  }
}