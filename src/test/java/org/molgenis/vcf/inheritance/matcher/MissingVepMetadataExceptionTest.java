package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.MissingVepMetadataException;

class MissingVepMetadataExceptionTest {

  @Test
  void getMessage() {
    assertEquals(
        "VEP metadata is missing in metadata json, vep id: 'CSQ'.",
        new MissingVepMetadataException("CSQ").getMessage());
  }
}
