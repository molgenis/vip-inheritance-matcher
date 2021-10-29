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
import org.molgenis.vcf.inheritance.matcher.model.Sample;


public class PedToSamplesMapper {

  private PedToSamplesMapper() {
  }

  public static Map<String, Map<String, Sample>> mapPedFileToPersons(List<Path> pedigreePaths) {
    Map<String, Map<String, Sample>> persons = new HashMap<>();
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

  private static Map<String, Map<String, Sample>> parse(PedReader reader) {
    final Map<String, Map<String, Sample>> pedigreePersons = new HashMap<>();
    StreamSupport.stream(Spliterators.spliteratorUnknownSize(reader.iterator(), 0), false)
        .map(PedToSamplesMapper::map)
        .forEach(sample -> addFamilyToMap(sample, pedigreePersons));
    return pedigreePersons;
  }

  private static Map<String, Sample> addFamilyToMap(Sample sample,
      Map<String, Map<String, Sample>> pedigreePersons) {

    return pedigreePersons.put(sample.getFamilyId(), addToFamily(sample, pedigreePersons));
  }

  private static Map<String, Sample> addToFamily(Sample sample,
      Map<String, Map<String, Sample>> pedigreePersons) {
    Map<String, Sample> family;
    if (pedigreePersons.containsKey(sample.getFamilyId())) {
      family = pedigreePersons.get(sample.getFamilyId());
    } else {
      family = new HashMap<>();
    }
    family.put(sample.getIndividualId(), sample);
    return family;
  }


  static Sample map(PedIndividual pedIndividual) {
    return Sample.builder().individualId(pedIndividual.getId())
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
