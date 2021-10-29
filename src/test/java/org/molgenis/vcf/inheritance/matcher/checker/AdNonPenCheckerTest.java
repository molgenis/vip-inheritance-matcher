package org.molgenis.vcf.inheritance.matcher.checker;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
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
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.util.PedigreeTestUtil;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AdNonPenCheckerTest {

  @Mock
  private VepMapper vepMapper;

  @ParameterizedTest(name = "{index} {4}")
  @MethodSource("provideTestCases")
  void check(VariantContext variantContext, Map<String, Sample> family, String gene,
      boolean expected,
      String displayName) {
    AdNonPenetranceChecker adNonPenChecker = new AdNonPenetranceChecker(vepMapper);
    if(gene.equals("GENE2")) {
      when(vepMapper
          .getNonPenetranceGenesForVariant(variantContext, singleton("GENE2")))
          .thenReturn(singleton("GENE2"));
    }else if(gene.equals("GENE1")){
      when(vepMapper
          .getNonPenetranceGenesForVariant(variantContext, singleton("GENE2")))
          .thenReturn(Collections.emptySet());
    }
    assertEquals(expected,
        !adNonPenChecker.check(variantContext, family, singleton("GENE2")).isEmpty());
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
      String gene = line[8];
      boolean expected = Boolean.parseBoolean(line[9]);

      Map<String, Sample> family = PedigreeTestUtil
          .createFamily(probandSex, probandAffectedStatus, fatherAffectedStatus,
              motherAffectedStatus, "FAM001");
      return Arguments.of(VariantContextTestUtil
          .createVariantContext(Arrays.asList(createGenotype("Patient", probandGt),
              createGenotype("Father", fatherGt),
              createGenotype("Mother", motherGt)),
              ""), family, gene, expected, testName);

    });
  }
}