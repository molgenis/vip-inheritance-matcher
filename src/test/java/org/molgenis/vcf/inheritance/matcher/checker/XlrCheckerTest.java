package org.molgenis.vcf.inheritance.matcher.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.mapExpectedString;

import htsjdk.variant.variantcontext.Genotype;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.util.ResourceUtils;

class XlrCheckerTest {

  final XlrChecker xlrChecker = new XlrChecker();

  @ParameterizedTest(name = "{index} {3}")
  @MethodSource("provideTestCases")
  void check(VcfRecord vcfRecord, Pedigree family, String expectedString,
             String displayName) {
    MatchEnum expected = mapExpectedString(expectedString);
    assertEquals(expected, xlrChecker.check(vcfRecord, family));
  }

  private static Stream<Arguments> provideTestCases() throws IOException {
    File testFile = ResourceUtils.getFile("classpath:XlrTests.tsv");
    List<String[]> lines = Files.lines(testFile.toPath())
        .map(line -> line.split("\t")).toList();

    return lines.stream().skip(1).map(line -> {
      String testName = line[0];
      String probandGt = line[1];
      AffectedStatus probandAffectedStatus = AffectedStatus.valueOf(line[2]);
      Sex probandSex = Sex.valueOf(line[3]);
      String fatherGt = line[4];
      AffectedStatus fatherAffectedStatus = AffectedStatus.valueOf(line[5]);
      String motherGt = line[6];
      AffectedStatus motherAffectedStatus = AffectedStatus.valueOf(line[7]);
      String brotherGt = line[8];
      AffectedStatus brotherAffectedStatus =
          line[9].isEmpty() ? null : AffectedStatus.valueOf(line[9]);
      String expected = line[10];

      Pedigree family = PedigreeTestUtil
          .createFamily(probandSex, probandAffectedStatus, fatherAffectedStatus,
              motherAffectedStatus, brotherAffectedStatus, "FAM001");
      List<Genotype> genotypes = new ArrayList<>();
      genotypes.add(createGenotype("Patient", probandGt));
      if (!fatherGt.isEmpty()) {
        genotypes.add(createGenotype("Father", fatherGt));
      }
      if (!motherGt.isEmpty()) {
        genotypes.add(createGenotype("Mother", motherGt));
      }
      if (!brotherGt.isEmpty()) {
        genotypes.add(createGenotype("Brother", brotherGt));
      }
      return Arguments.of(VariantContextTestUtil
          .createVariantContext(genotypes, "","X"), family, expected, testName);

    });
  }
}