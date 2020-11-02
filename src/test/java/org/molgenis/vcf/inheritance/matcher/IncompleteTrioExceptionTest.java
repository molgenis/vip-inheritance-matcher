package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IncompleteTrioExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Trio for sample 'Jan' is incomplete.",
        new IncompleteTrioException("Jan").getMessage());
  }
}