package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.MissingInfoException;

class MissingInfoExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Input is missing INFO field 'VEP'.",
        new MissingInfoException("VEP").getMessage());
  }
}