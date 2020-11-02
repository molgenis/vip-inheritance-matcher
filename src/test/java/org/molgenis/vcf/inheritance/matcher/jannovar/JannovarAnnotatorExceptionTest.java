package org.molgenis.vcf.inheritance.matcher.jannovar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.charite.compbio.jannovar.mendel.bridge.CannotAnnotateMendelianInheritance;
import org.junit.jupiter.api.Test;

class JannovarAnnotatorExceptionTest {

  @Test
  void getMessage() {
    CannotAnnotateMendelianInheritance exception = mock(CannotAnnotateMendelianInheritance.class);
    when(exception.getMessage()).thenReturn("test message");
    assertEquals(
        "An error occured while running the Jannovar mendelian annotator:'test message'",
        new JannovarAnnotatorException(exception).getMessage());
  }
}