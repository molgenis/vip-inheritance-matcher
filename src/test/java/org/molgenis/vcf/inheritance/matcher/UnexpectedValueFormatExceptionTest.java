package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnexpectedValueFormatExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Unexpected GENMOD value format, expecting 'FAMILY_ID:values'.",
        new UnexpectedValueFormatException("GENMOD").getMessage());
  }
}