package org.molgenis.vcf.inheritance.matcher.util;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class PedigreeTestUtil {

  public static Map<String, Sample> createFamily(Sex probandSex,
      AffectedStatus probandAffectedStatus, AffectedStatus fatherAffectedStatus,
      AffectedStatus motherAffectedStatus, String familyId) {
    Map<String, Sample> result = new HashMap<>();
    result.put("Patient", createSample(probandSex, probandAffectedStatus, familyId, "Patient", "Father",
        "Mother"));
    if (motherAffectedStatus != null) {
      result.put("Mother",createSample(Sex.FEMALE, motherAffectedStatus, familyId, "Mother", "", ""));
    }
    if (fatherAffectedStatus != null) {
      result.put("Father",createSample(Sex.MALE, fatherAffectedStatus, familyId, "Father", "", ""));
    }
    return result;
  }

  public static Sample createSample(Sex sex, AffectedStatus affectedStatus, String familyId, String patient, String father, String mother) {
    return Sample.builder().individualId(patient).familyId(familyId).paternalId(father)
        .maternalId(mother).sex(sex).affectedStatus(affectedStatus).proband(true)
        .build();
  }
}
