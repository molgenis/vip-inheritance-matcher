package org.molgenis.vcf.inheritance.matcher.jannovar;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;
import java.util.Collection;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

public class JannovarPedigreeMapper {

  private JannovarPedigreeMapper() {
  }

  public static Pedigree map(Trio trio) {
    Collection<Person> persons = mapPersons(trio);
    return new Pedigree(trio.getFamily(), persons);
  }

  private static Collection<Person> mapPersons(Trio trio) {
    Person father = mapPerson(trio.getFather(), null, null);
    Person mother = mapPerson(trio.getMother(), null, null);
    Person proband = mapPerson(trio.getProband(), father, mother);
    return Set.of(proband, father, mother);
  }

  private static Person mapPerson(Sample sample, Person father, Person mother) {
    org.molgenis.vcf.inheritance.matcher.model.Person person = sample.getPerson();
    String name = person.getIndividualId();
    Sex sex = mapSex(person.getSex());
    Disease affected = mapDisease(person.getAffectedStatus());
    return new Person(name, father, mother, sex, affected);
  }

  private static Disease mapDisease(AffectedStatus affectedStatus) {
    Disease disease;
    switch (affectedStatus) {
      case AFFECTED:
        disease = Disease.AFFECTED;
        break;
      case UNAFFECTED:
        disease = Disease.UNAFFECTED;
        break;
      default:
        disease = Disease.UNKNOWN;
    }
    return disease;
  }

  private static Sex mapSex(org.molgenis.vcf.inheritance.matcher.model.Sex sex) {
    Sex result;
    switch (sex) {
      case MALE:
        result = Sex.MALE;
        break;
      case FEMALE:
        result = Sex.FEMALE;
        break;
      default:
        result = Sex.UNKNOWN;
    }
    return result;
  }
}
