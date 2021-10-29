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
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PedToSamplesMapperTest {

  @Test
  void mapPedFileToPersons() throws FileNotFoundException {
    Path pedFile1 = ResourceUtils.getFile("classpath:example.ped").toPath();
    Path pedFile2 = ResourceUtils.getFile("classpath:example2.ped").toPath();
    List<Path> paths = Arrays.asList(pedFile1, pedFile2);

    Map<String, Map<String, Sample>> expected = new HashMap();
    expected.put("FAM001", Map.of(
        "John",
        Sample.builder()
            .familyId("FAM001")
            .individualId("John").paternalId("Jimmy").maternalId("Jane").sex(MALE)
            .affectedStatus(AffectedStatus.AFFECTED).build(),
        "Jimmy",
        Sample.builder()
            .familyId("FAM001")
            .individualId("Jimmy").paternalId("0").maternalId("0").sex(MALE)
            .affectedStatus(AffectedStatus.UNAFFECTED).build(),
        "Jane",
        Sample.builder()
            .familyId("FAM001")
            .individualId("Jane").paternalId("0").maternalId("0").sex(FEMALE)
            .affectedStatus(AffectedStatus.UNAFFECTED).build()));

    expected.put("FAM002",
        Map.of("James",
            Sample.builder()
                .familyId("FAM002")
                .individualId("James").paternalId("0").maternalId("0").sex(MALE)
                .affectedStatus(AffectedStatus.UNAFFECTED).build()));
    expected.put("FAM003", Map.of(
        "Jake",
        Sample.builder()
            .familyId("FAM003")
            .individualId("Jake").paternalId("0").maternalId("0").sex(MALE)
            .affectedStatus(AffectedStatus.AFFECTED).build()));

    assertEquals(expected, PedToSamplesMapper.mapPedFileToPersons(paths));
  }
}
