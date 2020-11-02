package org.molgenis.vcf.inheritance.matcher.jannovar;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import de.charite.compbio.jannovar.mendel.bridge.CannotAnnotateMendelianInheritance;

public class JannovarAnnotatorException extends RuntimeException {
  public JannovarAnnotatorException(
      CannotAnnotateMendelianInheritance cannotAnnotateMendelianInheritance) {
    super(format("An error occured while running the Jannovar mendelian annotator:'%s'", cannotAnnotateMendelianInheritance.getMessage()));
  }
}
