package org.molgenis.vcf.inheritance.matcher.model;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Settings {

  Path inputVcfPath;
  Path outputPath;
  boolean overwrite;
  boolean debug;
}
