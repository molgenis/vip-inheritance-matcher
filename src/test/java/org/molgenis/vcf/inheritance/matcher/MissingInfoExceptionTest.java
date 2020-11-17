package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MissingInfoExceptionTest {
  @Test
  void getMessage() {
    assertEquals(
        "Input is missing INFO field 'VEP'.",
        new MissingInfoException("VEP").getMessage());
  }
}