package org.molgenis.vcf.inheritance.matcher.ped;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PedigreeMapperTest {

  @Test
  void mapPedFileToPersons() throws FileNotFoundException {
    Path pedFile1 = ResourceUtils.getFile("classpath:example.ped").toPath();
    Path pedFile2 = ResourceUtils.getFile("classpath:example2.ped").toPath();
    List<Path> paths = Arrays.asList(pedFile1, pedFile2);

    Trio.TrioBuilder expectedBuilder = Trio.builder();
    expectedBuilder.family("FAM001");
        expectedBuilder.proband(
        Sample.builder()
            .person(new Person("FAM001", "John", "Jimmy", "Jane", MALE, AffectedStatus.AFFECTED))
            .index(0)
            .build());
    expectedBuilder.father(
        Sample.builder()
            .person(new Person("FAM001", "Jimmy", "0", "0", MALE, AffectedStatus.UNAFFECTED))
            .index(1)
            .build());
    expectedBuilder.mother(
        Sample.builder()
            .person(new Person("FAM001", "Jane", "0", "0", FEMALE, AffectedStatus.UNAFFECTED))
            .index(2)
            .build());

    assertEquals(Collections.singletonMap("John",expectedBuilder.build()), PedigreeMapper.map(paths, Arrays.asList("John","Jimmy","Jane","James","Jake")));
  }

  @Test
  void mapPedFileToPersonsMissingSamples() throws FileNotFoundException {
    Path pedFile1 = ResourceUtils.getFile("classpath:example.ped").toPath();
    List<Path> paths = Collections.singletonList(pedFile1);

    Trio.TrioBuilder expectedBuilder = Trio.builder();
    expectedBuilder.family("FAM001");
    expectedBuilder.proband(
        Sample.builder()
            .person(new Person("FAM001", "John", "Jimmy", "Jane", MALE, AffectedStatus.AFFECTED))
            .index(0)
            .build());
    expectedBuilder.father(
        Sample.builder()
            .person(new Person("FAM001", "Jimmy", "0", "0", MALE, AffectedStatus.UNAFFECTED))
            .index(1)
            .build());
    expectedBuilder.mother(
        Sample.builder()
            .person(new Person("FAM001", "Jane", "0", "0", FEMALE, AffectedStatus.UNAFFECTED))
            .index(-1)
            .build());

    assertEquals(Collections.singletonMap("John",expectedBuilder.build()), PedigreeMapper.map(paths, Arrays.asList("John","Jimmy")));
  }
}
