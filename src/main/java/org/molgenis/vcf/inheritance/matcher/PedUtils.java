package org.molgenis.vcf.inheritance.matcher;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.FileReader;
import java.io.IOException;
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
    try (CSVReader reader = new CSVReaderBuilder(new FileReader(path.toFile()))
        .withCSVParser(getParser()).build()) {
      for (String[] line : reader.readAll()) {
        if (line.length == 6) {
          if (probands.isEmpty() || probands.contains(line[SAMPLE_INDEX])) {
            result.put(line[SAMPLE_INDEX], line[FAMILY_INDEX]);
          }
        } else {
          throw new MalformedPedException(path.getFileName().toString());
        }
      }
    } catch (IOException | CsvException e) {
      throw new CsvReaderException(path.getFileName().toString(),e);
    }
    return result;
  }

  private static CSVParser getParser() {
    return new CSVParserBuilder().withSeparator('\t').build();
  }

}
