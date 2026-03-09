package org.molgenis.vcf.inheritance.matcher.util;

import static java.util.Objects.requireNonNull;

import org.molgenis.vcf.inheritance.matcher.*;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.springframework.stereotype.Component;

/** Quirky class to enable use of local {@link org.molgenis.vcf.utils.vep.VepMetadataService} */
@Component
public class InheritanceServiceFactoryImpl implements InheritanceServiceFactory {
  private final Annotator annotator;
  private final PedigreeInheritanceChecker pedigreeInheritanceChecker;

  public InheritanceServiceFactoryImpl(
      Annotator annotator, PedigreeInheritanceChecker pedigreeInheritanceChecker) {
    this.annotator = requireNonNull(annotator);
    this.pedigreeInheritanceChecker = requireNonNull(pedigreeInheritanceChecker);
  }

  @Override
  public InheritanceService create(Settings settings) {
    return new InheritanceService(
        annotator, pedigreeInheritanceChecker, settings.getInputPedPaths(), settings.getProbands());
  }
}
