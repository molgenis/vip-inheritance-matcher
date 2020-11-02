package org.molgenis.vcf.inheritance.matcher.ped;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DuplicateSampleExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "Sample with id 'Jan' is present in multiple pedigree files.",
        new DuplicateSampleException("Jan").getMessage());
  }
}