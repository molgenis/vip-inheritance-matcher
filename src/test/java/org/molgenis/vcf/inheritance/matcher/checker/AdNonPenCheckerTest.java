package org.molgenis.vcf.inheritance.matcher.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AdNonPenCheckerTest {

  @Mock
  private VepMapper vepMapper;

  @ParameterizedTest(name = "{index} {4}")
  @MethodSource("provideTestCases")
  void check(VariantContext variantContext, Pedigree family,
      boolean isIncompletePenetrance, boolean expected,
      String displayName) {
    AdNonPenetranceChecker adNonPenChecker = new AdNonPenetranceChecker(vepMapper);
    when(vepMapper
        .containsIncompletePenetrance(variantContext))
        .thenReturn(isIncompletePenetrance);
    assertEquals(expected, adNonPenChecker.check(variantContext, family));
  }

  private static Stream<Arguments> provideTestCases() throws IOException {
    File testFile = ResourceUtils.getFile("classpath:ADNonPenTests.tsv");
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
      boolean isIncompletePenetrance = Boolean.parseBoolean(line[10]);
      String brotherGt = line[8];
      AffectedStatus brotherAffectedStatus =
          line[9].isEmpty() ? null : AffectedStatus.valueOf(line[9]);
      boolean expected = Boolean.parseBoolean(line[11]);

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
          .createVariantContext(genotypes,
              ""), family, isIncompletePenetrance, expected, testName);

    });
  }
}