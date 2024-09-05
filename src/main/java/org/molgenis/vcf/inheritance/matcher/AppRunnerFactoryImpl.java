package org.molgenis.vcf.inheritance.matcher;

import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.util.InheritanceServiceFactory;
import org.molgenis.vcf.inheritance.matcher.util.VepMetadataServiceFactory;
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
  private final VepMetadataServiceFactory vepMetadataServiceFactory;

  AppRunnerFactoryImpl(
          VcfReaderFactory vcfReaderFactory,
          RecordWriterFactory recordWriterFactory,
          InheritanceServiceFactory inheritanceServiceFactory,
          VepMetadataServiceFactory vepMetadataServiceFactory) {
    this.vcfReaderFactory = requireNonNull(vcfReaderFactory);
    this.recordWriterFactory = requireNonNull(recordWriterFactory);
    this.inheritanceServiceFactory = inheritanceServiceFactory;
    this.vepMetadataServiceFactory = vepMetadataServiceFactory;
  }

  // Suppress 'Resources should be closed'
  @SuppressWarnings("java:S2095")
  @Override
  public AppRunner create(Settings settings) {
    VcfReader vcfReader = vcfReaderFactory.create(settings);
    VepMetadata vepMetadata = new VepMetadata(vcfReader.getFileHeader(), vepMetadataServiceFactory.create());
    try {
      RecordWriter recordWriter = recordWriterFactory.create(settings);
      return new AppRunnerImpl(vcfReader, recordWriter, inheritanceServiceFactory.create(settings, vepMetadata));
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
