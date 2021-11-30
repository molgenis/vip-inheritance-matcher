package org.molgenis.vcf.inheritance.matcher.checker;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
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
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.util.PedigreeTestUtil;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class ArCompoundCheckerTest {

  @Mock
  private VepMapper vepMapper;
  private final static Gene gene1 = new Gene("GENE1", "EntrezGene", false, singleton(InheritanceMode.AR));

  @ParameterizedTest(name = "{index} {4}")
  @MethodSource("provideTestCases")
  void check(VariantContext variantContext, Map<String, List<VariantContext>> geneVariantMap,
      Pedigree family, boolean expected,
      String displayName) {
    ArCompoundChecker arCompoundChecker = new ArCompoundChecker(vepMapper);
    when(vepMapper.getGenes(variantContext)).thenReturn(singletonMap("GENE1", gene1));
    assertEquals(expected, !arCompoundChecker.check(geneVariantMap, variantContext, family).isEmpty());
  }

  private static Stream<Arguments> provideTestCases() throws IOException {
    File testFile = ResourceUtils.getFile("classpath:ArCompoundTests.tsv");
    List<String[]> lines = Files.lines(testFile.toPath())
        .map(line -> line.split("\t")).collect(Collectors.toList());

    return lines.stream().skip(1).map(line -> {
      String testName = line[0];
      String probandGt = line[1];
      String probandOtherGt = line[2];
      AffectedStatus probandAffectedStatus = AffectedStatus.valueOf(line[3]);
      Sex probandSex = Sex.valueOf(line[4]);
      String fatherGt = line[5];
      String fatherOtherGt = line[6];
      AffectedStatus fatherAffectedStatus = null;
      if (!line[7].isEmpty()) {
        fatherAffectedStatus = AffectedStatus.valueOf(line[7]);
      }
      String motherGt = line[8];
      String motherOtherGt = line[9];
      AffectedStatus motherAffectedStatus = null;
      if (!line[10].isEmpty()) {
        motherAffectedStatus = AffectedStatus.valueOf(line[10]);
      }
      String brotherGt = line[11];
      String brotherOtherGt = line[12];
      AffectedStatus brotherAffectedStatus =
          line[13].isEmpty() ? null : AffectedStatus.valueOf(line[13]);
      boolean expected = Boolean.parseBoolean(line[14]);

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

      List<Genotype> otherGenotypes = new ArrayList<>();
      otherGenotypes.add(createGenotype("Patient", probandOtherGt));
      if (!fatherGt.isEmpty()) {
        otherGenotypes.add(createGenotype("Father", fatherOtherGt));
      }
      if (!motherGt.isEmpty()) {
        otherGenotypes.add(createGenotype("Mother", motherOtherGt));
      }
      if (!brotherGt.isEmpty()) {
        otherGenotypes.add(createGenotype("Brother", brotherOtherGt));
      }

      return Arguments.of(VariantContextTestUtil
              .createVariantContext(genotypes,
                  ""),
          singletonMap("GENE1", singletonList(VariantContextTestUtil
              .createVariantContext(otherGenotypes,
                  ""))), family, expected, testName);
    });
  }
}