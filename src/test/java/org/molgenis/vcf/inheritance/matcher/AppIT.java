package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir
  Path sharedTempDir;

  @Test
  void test() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:integration.vcf").toString();
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected.vcf").toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }
}
