package org.molgenis.vcf.inheritance.matcher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {

  private PathUtils() {
  }

  public static List<Path> parsePaths(String optionValue) {
    List<Path> result = new ArrayList<>();
    String[] paths = optionValue.split(",");
    for (String path : paths) {
      result.add(Path.of(path));
    }
    return result;
  }
}
