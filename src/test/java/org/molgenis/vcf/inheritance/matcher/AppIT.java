package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

class AppIT {

  @TempDir
  Path sharedTempDir;

  @ParameterizedTest
  @ValueSource(strings = {"_mf_unaff","_m_unaff_f_aff","_mf_aff"})
  void test(String postfix) throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:input.vcf").toString();
    String pedFile = ResourceUtils.getFile(String.format("classpath:pedigree%s.ped", postfix)).toString();
    String outputFile = sharedTempDir.resolve("example.out.vcf").toString();

    String[] args = {"-i", inputFile, "-pd", pedFile, "-pb", "Patient", "-o", outputFile};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));
    Path expectedOutputFile = ResourceUtils.getFile(String.format("classpath:expected%s.vcf", postfix)).toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }
}
