package org.molgenis.vcf.inheritance.matcher;

import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.util.InheritanceServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
class AppRunnerFactoryImpl implements AppRunnerFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppRunnerFactoryImpl.class);

  private final VcfReaderFactory vcfReaderFactory;
  private final RecordWriterFactory recordWriterFactory;
  private final InheritanceServiceFactory inheritanceServiceFactory;

  AppRunnerFactoryImpl(
          VcfReaderFactory vcfReaderFactory,
          RecordWriterFactory recordWriterFactory,
          InheritanceServiceFactory inheritanceServiceFactory) {
    this.vcfReaderFactory = requireNonNull(vcfReaderFactory);
    this.recordWriterFactory = requireNonNull(recordWriterFactory);
    this.inheritanceServiceFactory = inheritanceServiceFactory;
  }

  // Suppress 'Resources should be closed'
  @SuppressWarnings("java:S2095")
  @Override
  public AppRunner create(Settings settings) {
    VcfReader vcfReader = vcfReaderFactory.create(settings);
    try {
      RecordWriter recordWriter = recordWriterFactory.create(settings);
      return new AppRunnerImpl(vcfReader, recordWriter, inheritanceServiceFactory.create(settings));
    } catch (Exception e) {
      try {
        vcfReader.close();
      } catch (Exception closeException) {
        LOGGER.warn("error closing vcf reader", closeException);
      }
      throw e;
    }
  }
}
