package org.molgenis.vcf.inheritance.matcher.checker;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;

public class PedigreeTestUtil {

  public static Pedigree createFamily(Sex probandSex,
      AffectedStatus probandAffectedStatus, AffectedStatus fatherAffectedStatus,
      AffectedStatus motherAffectedStatus, String familyId) {
    return createFamily(probandSex, probandAffectedStatus, fatherAffectedStatus, motherAffectedStatus, null, familyId);
  }

  public static Pedigree createFamily(Sex probandSex,
      AffectedStatus probandAffectedStatus, AffectedStatus fatherAffectedStatus,
      AffectedStatus motherAffectedStatus, AffectedStatus brotherAffectedStatus, String familyId) {
    Map<String, Sample> result = new HashMap<>();
    result.put("Patient", createSample(probandSex, probandAffectedStatus, familyId, "Patient", "Father",
        "Mother"));
    if (motherAffectedStatus != null) {
      result.put("Mother",createSample(Sex.FEMALE, motherAffectedStatus, familyId, "Mother", "", ""));
    }
    if (fatherAffectedStatus != null) {
      result.put("Father",createSample(Sex.MALE, fatherAffectedStatus, familyId, "Father", "", ""));
    }
    if (brotherAffectedStatus != null) {
      result.put("Brother",createSample(Sex.MALE, brotherAffectedStatus, familyId, "Brother", "Father", "Mother"));
    }
    return Pedigree.builder().id(familyId).members(result).build();
  }

  public static Sample createSample(Sex sex, AffectedStatus affectedStatus, String familyId, String patient, String father, String mother) {
    Person person =  Person.builder().individualId(patient).familyId(familyId).paternalId(father)
        .maternalId(mother).sex(sex).affectedStatus(affectedStatus)
        .build();
    return Sample.builder().proband(true).person(person).build();
  }
}
