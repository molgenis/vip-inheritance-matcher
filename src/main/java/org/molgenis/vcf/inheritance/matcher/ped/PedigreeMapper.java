package org.molgenis.vcf.inheritance.matcher.ped;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.molgenis.vcf.inheritance.matcher.ped.PedIndividual.AffectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PedigreeMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(PedigreeMapper.class);

  private PedigreeMapper() {
  }

  public static Map<String, Trio> map(List<Path> pedigreePaths, List<String> sampleIds) {
    Map<String, Trio> trios = new HashMap<>();
    Map<String, Sample> samples = new HashMap<>();
    for (Path pedigreePath : pedigreePaths) {
      try (PedReader reader = new PedReader(new FileReader(pedigreePath.toFile()))) {
        parse(reader, sampleIds, samples);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    for (Sample sample : samples.values()) {
      Person person = sample.getPerson();
      Sample father = samples.get(person.getPaternalId());
      Sample mother = samples.get(person.getMaternalId());
      if (mother != null && father != null) {
        trios.put(person.getIndividualId(),
            Trio.builder().family(sample.getPerson().getFamilyId()).proband(sample).father(father)
                .mother(mother).build());
      } else {
        LOGGER.debug("Incomplete trio for sample: {}", sample.getPerson().getIndividualId());
      }
    }
    return trios;
  }

  private static void parse(PedReader reader, List<String> samples,
      Map<String, Sample> pedigreeSamples) {
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.iterator(), 0), false)
        .map(PedigreeMapper::map).forEach(person -> {
      checkPerson(person, pedigreeSamples);
      pedigreeSamples.put(person.getIndividualId(),
          Sample.builder().person(person).index(samples.indexOf(person.getIndividualId()))
              .build());
    });
  }

  private static void checkPerson(Person person, Map<String, Sample> pedigreeSamples) {
    if (pedigreeSamples.containsKey(person.getIndividualId())) {
      throw new DuplicateSampleException(person.getIndividualId());
    }
  }

  private static Person map(PedIndividual pedIndividual) {
    return new Person(
        pedIndividual.getFamilyId(),
        pedIndividual.getId(),
        pedIndividual.getPaternalId(),
        pedIndividual.getMaternalId(),
        map(pedIndividual.getSex()),
        map(pedIndividual.getAffectionStatus()));
  }

  private static Sex map(PedIndividual.Sex sex) {
    switch (sex) {
      case MALE:
        return Sex.MALE;
      case FEMALE:
        return Sex.FEMALE;
      case UNKNOWN:
        return Sex.UNKNOWN_SEX;
      default:
        return Sex.OTHER_SEX;
    }
  }

  private static AffectedStatus map(AffectionStatus affectionStatus) {
    switch (affectionStatus) {
      case AFFECTED:
        return AffectedStatus.AFFECTED;
      case UNAFFECTED:
        return AffectedStatus.UNAFFECTED;
      case UNKNOWN:
        return AffectedStatus.MISSING;
      default:
        return AffectedStatus.UNRECOGNIZED;
    }
  }
}
