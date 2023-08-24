package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashMap;
import java.util.Map;

public class InheritanceUtils {
    private InheritanceUtils() {
    }

    public static Pedigree filterBloodRelatives(Pedigree family, Sample sample) {
        Map<String, Sample> filteredFamily = new HashMap<>();
        filteredFamily.put(sample.getPerson().getIndividualId(), sample);
        addParents(family, sample, filteredFamily);
        addChildren(sample, family, filteredFamily);
        return Pedigree.builder().id(family.getId()).members(filteredFamily).build();
    }

    private static void addChildren(Sample proband, Pedigree family, Map<String, Sample> filteredFamily) {
        for (Sample sample : family.getMembers().values()) {
            Person person = sample.getPerson();
            Person probandPerson = proband.getPerson();
            if (person.getPaternalId().equals(probandPerson.getPaternalId()) && person.getMaternalId().equals(probandPerson.getMaternalId())
                    || filteredFamily.containsKey(person.getPaternalId()) && filteredFamily.containsKey(person.getMaternalId())) {
                filteredFamily.put(person.getIndividualId(), sample);
            }
        }
    }

    private static void addParents(Pedigree family, Sample sample, Map<String, Sample> sampleMap) {
        Person person = sample.getPerson();
        Sample father = family.getMembers().get(person.getPaternalId());
        Sample mother = family.getMembers().get(person.getMaternalId());
        if (father != null) {
            sampleMap.put(father.getPerson().getIndividualId(), father);
            addParents(family, father, sampleMap);
        }
        if (mother != null) {
            sampleMap.put(mother.getPerson().getIndividualId(), mother);
            addParents(family, mother, sampleMap);
        }
    }
}
