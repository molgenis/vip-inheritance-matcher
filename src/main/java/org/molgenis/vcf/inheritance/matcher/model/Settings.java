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
  List<String> probands;
  boolean overwrite;
  boolean debug;
  Set<String> nonPenetranceGenes;
  boolean strict;
}
