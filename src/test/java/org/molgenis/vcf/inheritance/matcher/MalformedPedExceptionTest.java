package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MalformedPedExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "PED file 'PED' is malformed, expecting 6 columns on every row.",
        new MalformedPedException("PED").getMessage());
  }
}