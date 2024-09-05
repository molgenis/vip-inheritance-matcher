package org.molgenis.vcf.inheritance.matcher.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.mapExpectedString;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.VepMetadata;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.util.ResourceUtils;

class DeNovoCheckerTest {
    final DeNovoChecker deNovoChecker = new DeNovoChecker();

    @ParameterizedTest(name = "{index} {3}")
    @MethodSource("provideTestCases")
    void check(VcfRecord vcfRecord, Pedigree family, String expectedString,
               String displayName) {
        MatchEnum expected = mapExpectedString(expectedString);
        Sample individual = family.getMembers().get("Patient");
        assertEquals(expected, deNovoChecker.checkDeNovo(vcfRecord, individual));
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
      String expected = line[6];

      Pedigree family = PedigreeTestUtil
          .createFamily(probandSex, AffectedStatus.MISSING, AffectedStatus.MISSING,
              AffectedStatus.MISSING, "FAM001");
      return Arguments.of(VariantContextTestUtil
          .createVariantContext(Arrays.asList(createGenotype("Patient", probandGt),
              createGenotype("Father", fatherGt),
              createGenotype("Mother", motherGt)),
                  new VepMetadata("CSQ",-1,-1,-1,-1,-1), "", chrom), family, expected, testName);

    });
  }
}