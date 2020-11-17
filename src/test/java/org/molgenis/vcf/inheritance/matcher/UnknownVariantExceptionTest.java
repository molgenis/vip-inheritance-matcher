package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnknownVariantExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Cannot find a variant for compound key '1_2_A_T'.",
        new UnknownVariantException("1_2_A_T").getMessage());
  }
}