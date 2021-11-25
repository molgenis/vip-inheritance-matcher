package org.molgenis.vcf.inheritance.matcher.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.util.PedigreeTestUtil;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AdCheckerTest {

  @ParameterizedTest(name = "{index} {3}")
  @MethodSource("provideTestCases")
  void check(VariantContext variantContext, Pedigree family, boolean expected,
      String displayName) {
    assertEquals(expected, AdChecker.check(variantContext, family));
  }

  private static Stream<Arguments> provideTestCases() throws IOException {
    File testFile = ResourceUtils.getFile("classpath:ADtests.tsv");
    List<String[]> lines = Files.lines(testFile.toPath())
        .map(line -> line.split("\t")).collect(Collectors.toList());

    return lines.stream().skip(1).map(line -> {
      String testName = line[0];
      String probandGt = line[1];
      AffectedStatus probandAffectedStatus = AffectedStatus.valueOf(line[2]);
      Sex probandSex = Sex.valueOf(line[3]);
      String fatherGt = line[4];
      AffectedStatus fatherAffectedStatus = AffectedStatus.valueOf(line[5]);
      String motherGt = line[6];
      AffectedStatus motherAffectedStatus = AffectedStatus.valueOf(line[7]);
      boolean expected = Boolean.parseBoolean(line[8]);

      Pedigree family = PedigreeTestUtil
          .createFamily(probandSex, probandAffectedStatus, fatherAffectedStatus,
              motherAffectedStatus, "FAM001");
      return Arguments.of(VariantContextTestUtil
          .createVariantContext(Arrays.asList(createGenotype("Patient", probandGt),
              createGenotype("Father", fatherGt),
              createGenotype("Mother", motherGt)),
              ""), family, expected, testName);

    });
  }
}