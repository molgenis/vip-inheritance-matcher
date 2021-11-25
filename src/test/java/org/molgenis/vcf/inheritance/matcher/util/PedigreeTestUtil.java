package org.molgenis.vcf.inheritance.matcher.util;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class PedigreeTestUtil {

  public static Pedigree createFamily(Sex probandSex,
      AffectedStatus probandAffectedStatus, AffectedStatus fatherAffectedStatus,
      AffectedStatus motherAffectedStatus, String familyId) {
    return createFamily(probandSex, probandAffectedStatus, fatherAffectedStatus, motherAffectedStatus, null, familyId);
  }

  public static Pedigree createFamily(Sex probandSex,
      AffectedStatus probandAffectedStatus, AffectedStatus fatherAffectedStatus,
      AffectedStatus motherAffectedStatus, AffectedStatus brotherAffectedStatus, String familyId) {
    Map<String, Individual> result = new HashMap<>();
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
    return new Pedigree(familyId, result);
  }

  public static Individual createSample(Sex sex, AffectedStatus affectedStatus, String familyId, String patient, String father, String mother) {
    return Individual.builder().id(patient).familyId(familyId).paternalId(father)
        .maternalId(mother).sex(sex).affectedStatus(affectedStatus).proband(true)
        .build();
  }
}
