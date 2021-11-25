package org.molgenis.vcf.inheritance.matcher;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.molgenis.vcf.inheritance.matcher.PedIndividual.AffectionStatus;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

public class PedToSamplesMapper {

  private PedToSamplesMapper() {
  }

  public static Map<String, Pedigree> mapPedFileToPersons(List<Path> pedigreePaths) {
    Map<String, Pedigree> persons = new HashMap<>();
    for (Path pedigreePath : pedigreePaths) {
      try (PedReader reader = new PedReader(new FileReader(pedigreePath.toFile()))) {
        persons.putAll(parse(reader));
      } catch (IOException e) {
        // this should never happen since the files were validated in the AppCommandLineOptions
        throw new IllegalStateException(e);
      }
    }
    return persons;
  }

  private static Map<String, Pedigree> parse(PedReader reader) {
    final Map<String, Pedigree> pedigreePersons = new HashMap<>();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.iterator(), 0), false)
        .map(PedToSamplesMapper::map)
        .forEach(sample -> addFamilyToMap(sample, pedigreePersons));
    return pedigreePersons;
  }

  private static Pedigree addFamilyToMap(Individual individual,
      Map<String, Pedigree> pedigreePersons) {

    return pedigreePersons.put(individual.getFamilyId(), addToFamily(individual, pedigreePersons));
  }

  private static Pedigree addToFamily(Individual individual,
      Map<String, Pedigree> pedigreePersons) {
    Pedigree family;
    if (pedigreePersons.containsKey(individual.getFamilyId())) {
      family = pedigreePersons.get(individual.getFamilyId());
    } else {
      family = new Pedigree(individual.getFamilyId(), new HashMap<>());
    }
    family.getMembers().put(individual.getId(), individual);
    return family;
  }


  static Individual map(PedIndividual pedIndividual) {
    return Individual.builder().id(pedIndividual.getId())
        .familyId(pedIndividual.getFamilyId()).paternalId(pedIndividual.getPaternalId())
        .maternalId(pedIndividual.getMaternalId())
        .affectedStatus(map(pedIndividual.getAffectionStatus())).sex(pedIndividual.getSex())
        .build();
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
