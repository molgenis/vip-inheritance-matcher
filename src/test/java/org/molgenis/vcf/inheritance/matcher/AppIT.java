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
  void testNoVep() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:integration_noVEPinheritance.vcf").toString();
    String pedigree = ResourceUtils.getFile("classpath:pedigree_complex.ped").toString();
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-o", outputFile, "-pd", pedigree};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected_noVEPinheritance.vcf").toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }

  @Test
  void testNoPed() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:integration.vcf").toString();
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-o", outputFile};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected_noPed.vcf").toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }

  @Test
  void testProband() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:integration.vcf").toString();
    String pedigree = ResourceUtils.getFile("classpath:pedigree_complex.ped").toString();
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-o", outputFile, "-pd", pedigree, "-pb", "Patient,Patient2"};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected_probands.vcf").toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }

  @Test
  void testNoParents() throws IOException {
    String inputFile = ResourceUtils.getFile("classpath:integration.vcf").toString();
    String pedigree = ResourceUtils.getFile("classpath:pedigree_fam_no_parents.ped").toString();
    String outputFile = sharedTempDir.resolve("actual.vcf").toString();

    String[] args = {"-i", inputFile, "-o", outputFile, "-pd", pedigree, "-pb", "Patient,Patient2"};
    SpringApplication.run(App.class, args);

    String outputVcf = Files.readString(Path.of(outputFile));

    Path expectedOutputFile = ResourceUtils.getFile("classpath:expected_noParents.vcf").toPath();
    String expectedOutputVcf = Files.readString(expectedOutputFile).replaceAll("\\R", "\n");

    assertEquals(expectedOutputVcf, outputVcf);
  }
}
