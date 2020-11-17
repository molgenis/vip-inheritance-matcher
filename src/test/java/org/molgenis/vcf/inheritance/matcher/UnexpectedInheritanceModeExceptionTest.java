package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnexpectedInheritanceModeExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Unexpected inheritance mode 'YR'.",
        new UnexpectedInheritanceModeException("YR").getMessage());
  }
}