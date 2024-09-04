package org.molgenis.vcf.inheritance.matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

class AppRunnerImpl implements AppRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppRunnerImpl.class);

  private final VcfReader vcfReader;
  private final RecordWriter recordWriter;
    private final InheritanceService inheritanceService;

    AppRunnerImpl(VcfReader vcfReader, RecordWriter recordWriter, InheritanceService inheritanceService) {
      this.vcfReader = requireNonNull(vcfReader);
      this.recordWriter = requireNonNull(recordWriter);
      this.inheritanceService = inheritanceService;
    }

  public void run() {
    LOGGER.info("Matching inheritance ...");
    inheritanceService.run(vcfReader, recordWriter);
    LOGGER.info("done");
  }

  @Override
  public void close() {
    try {
      recordWriter.close();
    } catch (Exception e) {
      LOGGER.error("error closing writer", e);
    }
    try {
      vcfReader.close();
    } catch (Exception e) {
      LOGGER.error("error closing reader", e);
    }
  }
}
