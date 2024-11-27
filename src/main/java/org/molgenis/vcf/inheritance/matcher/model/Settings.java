package org.molgenis.vcf.inheritance.matcher.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Settings {
  Path inputVcfPath;
  List<Path> inputPedPaths;
  Path outputPath;
  Path metadataPath;
  List<String> probands;
  Set<String> pathogenicClasses;
  boolean overwrite;
  boolean debug;
  boolean strict;
}
