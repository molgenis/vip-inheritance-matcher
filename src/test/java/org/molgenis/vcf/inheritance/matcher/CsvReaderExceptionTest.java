package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class CsvReaderExceptionTest {

  @Test
  void getMessage() {
    Exception ex = mock(Exception.class);
    when(ex.getMessage()).thenReturn("TEST");
    assertEquals(
        "An exception occurred while reading the PED file 'PED': TEST.",
        new CsvReaderException("PED", ex).getMessage());
  }
}