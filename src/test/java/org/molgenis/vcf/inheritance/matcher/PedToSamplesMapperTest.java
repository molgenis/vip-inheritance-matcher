package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PedToSamplesMapperTest {

  @Test
  void mapPedFileToPersons() throws FileNotFoundException {
    Path pedFile1 = ResourceUtils.getFile("classpath:example.ped").toPath();
    Path pedFile2 = ResourceUtils.getFile("classpath:example2.ped").toPath();
    List<Path> paths = Arrays.asList(pedFile1, pedFile2);

    Map<String, Pedigree> expected = new HashMap();
    expected.put("FAM001", new Pedigree("FAM001", Map.of(
        "John",
        Individual.builder()
            .familyId("FAM001")
            .id("John").paternalId("Jimmy").maternalId("Jane").sex(MALE)
            .affectedStatus(AffectedStatus.AFFECTED).build(),
        "Jimmy",
        Individual.builder()
            .familyId("FAM001")
            .id("Jimmy").paternalId("0").maternalId("0").sex(MALE)
            .affectedStatus(AffectedStatus.UNAFFECTED).build(),
        "Jane",
        Individual.builder()
            .familyId("FAM001")
            .id("Jane").paternalId("0").maternalId("0").sex(FEMALE)
            .affectedStatus(AffectedStatus.UNAFFECTED).build())));
    expected.put("FAM002",
        new Pedigree("FAM002", Map.of("James",
            Individual.builder()
                .familyId("FAM002")
                .id("James").paternalId("0").maternalId("0").sex(MALE)
                .affectedStatus(AffectedStatus.UNAFFECTED).build())));
    expected.put("FAM003", new Pedigree("FAM003", Map.of(
        "Jake",
        Individual.builder()
            .familyId("FAM003")
            .id("Jake").paternalId("0").maternalId("0").sex(MALE)
            .affectedStatus(AffectedStatus.AFFECTED).build())));

    assertEquals(expected, PedToSamplesMapper.mapPedFileToPersons(paths));
  }
}
