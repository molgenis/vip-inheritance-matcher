package org.molgenis.vcf.inheritance.matcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedUtils {

  private static final int FAMILY_INDEX = 0;
  private static final int SAMPLE_INDEX = 1;

  private PedUtils() {
  }

  static Map<String, String> map(List<Path> paths, List<String> probands) {
    Map<String, String> result = new HashMap<>();
    for (Path path : paths) {
      result.putAll(map(path, probands));
    }
    return result;
  }

  private static Map<String, String> map(Path path, List<String> probands) {
    Map<String, String> result = new HashMap<>();
    try (BufferedReader buffer = new BufferedReader(new FileReader(path.toFile()))) {
      buffer.lines().forEach(line -> {
        String[] tokens = line.split("\\s+");
        if (tokens.length != 0 && !tokens[0].startsWith("#")) {
          if (tokens.length == 6) {
            if (probands.isEmpty() || probands.contains(tokens[SAMPLE_INDEX])) {
              result.put(tokens[SAMPLE_INDEX], tokens[FAMILY_INDEX]);
            }
          } else {
            throw new MalformedPedException(path.getFileName().toString());
          }
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException(path.getFileName().toString(), e);
    }
    return result;
  }
}
