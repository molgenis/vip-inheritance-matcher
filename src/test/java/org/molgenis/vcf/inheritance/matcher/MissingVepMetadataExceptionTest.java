package org.molgenis.vcf.inheritance.matcher;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.MissingVepMetadataException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MissingVepMetadataExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "VEP metadata is missing in metadata json, vep id: 'CSQ'.",
        new MissingVepMetadataException("CSQ").getMessage());
  }
}