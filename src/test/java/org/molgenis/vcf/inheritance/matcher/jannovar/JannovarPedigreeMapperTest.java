package org.molgenis.vcf.inheritance.matcher.jannovar;

import static org.junit.jupiter.api.Assertions.*;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Sex;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

@ExtendWith(MockitoExtension.class)
class JannovarPedigreeMapperTest {

  Trio trio;

  @BeforeEach
  void setUp() {
    Trio.TrioBuilder trioBuilder = Trio.builder();
    trioBuilder.family("FAM001");
    trioBuilder.proband(
        Sample.builder()
            .person(new Person("FAM001", "John", "Jimmy", "Jane", MALE, AffectedStatus.AFFECTED))
            .index(0)
            .build());
    trioBuilder.father(
        Sample.builder()
            .person(new Person("FAM001", "Jimmy", "0", "0", MALE, AffectedStatus.UNAFFECTED))
            .index(1)
            .build());
    trioBuilder.mother(
        Sample.builder()
            .person(new Person("FAM001", "Jane", "0", "0", FEMALE, AffectedStatus.UNAFFECTED))
            .index(2)
            .build());
    trio = trioBuilder.build();
  }

  @Test
  void map() {
    de.charite.compbio.jannovar.pedigree.Person mother = new de.charite.compbio.jannovar.pedigree.Person(
        "Jane", null, null, Sex.MALE, Disease.UNAFFECTED, Collections.emptyList());
    de.charite.compbio.jannovar.pedigree.Person father = new de.charite.compbio.jannovar.pedigree.Person(
        "Jimmy", null, null, Sex.FEMALE, Disease.UNAFFECTED, Collections.emptyList());
    de.charite.compbio.jannovar.pedigree.Person proband = new de.charite.compbio.jannovar.pedigree.Person(
        "John", father, mother, Sex.MALE, Disease.AFFECTED, Collections.emptyList());
    Pedigree expected = new Pedigree("FAM001", Set.of(proband, father, mother));
    Pedigree actual = JannovarPedigreeMapper.map(trio);
    //assertEquals(expected, actual) results in failure due to missing equals for IndexedPerson, an inner class in the Pedigree
    assertAll(
        () -> assertEquals(actual.getNMembers(), expected.getNMembers()),
        () -> assertTrue(actual.getNames().containsAll(expected.getNames())),
        () -> assertEquals(actual.getName(), expected.getName()));
  }
}