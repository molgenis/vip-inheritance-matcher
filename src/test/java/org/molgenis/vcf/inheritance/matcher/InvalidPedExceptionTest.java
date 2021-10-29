package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InvalidPedExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Invalid PED line, expected 6 columns on line: 'Patient\tFather\tMother\t0'.",
        new InvalidPedException("Patient\tFather\tMother\t0").getMessage());
  }
}