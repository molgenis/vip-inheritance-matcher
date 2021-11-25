package org.molgenis.vcf.inheritance.matcher.checker;

import static org.junit.jupiter.api.Assertions.*;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.util.PedigreeTestUtil;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.springframework.util.ResourceUtils;

class DeNovoCheckerTest {


    @ParameterizedTest(name = "{index} {3}")
    @MethodSource("provideTestCases")
    void check(VariantContext variantContext, Pedigree family, boolean expected,
    String displayName) {
      Individual individual = family.getMembers().get("Patient");
      assertEquals(expected, DeNovoChecker.checkDeNovo(variantContext, family, individual));
    }

  private static Stream<Arguments> provideTestCases() throws IOException {
    File testFile = ResourceUtils.getFile("classpath:DenovoTests.tsv");
    List<String[]> lines = Files.lines(testFile.toPath())
        .map(line -> line.split("\t")).collect(Collectors.toList());

    return lines.stream().skip(1).map(line -> {
      String testName = line[0];
      String probandGt = line[1];
      Sex probandSex = Sex.valueOf(line[2]);
      String fatherGt = line[3];
      String motherGt = line[4];
      String chrom = line[5];
      boolean expected = Boolean.parseBoolean(line[6]);

      Pedigree family = PedigreeTestUtil
          .createFamily(probandSex, AffectedStatus.MISSING, AffectedStatus.MISSING,
              AffectedStatus.MISSING, "FAM001");
      return Arguments.of(VariantContextTestUtil
          .createVariantContext(Arrays.asList(createGenotype("Patient", probandGt),
              createGenotype("Father", fatherGt),
              createGenotype("Mother", motherGt)),
              "", chrom), family, expected, testName);

    });
  }
}