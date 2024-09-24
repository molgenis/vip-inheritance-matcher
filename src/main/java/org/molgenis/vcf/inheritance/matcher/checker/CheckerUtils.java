package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

public class CheckerUtils {
    public static Map<AffectedStatus, Set<Sample>> getMembersByStatus(Pedigree family) {
        Map<AffectedStatus, Set<Sample>> membersByStatus = new HashMap<>();
        Set<Sample> affected = new HashSet<>();
        Set<Sample> unAffected = new HashSet<>();
        Set<Sample> missing = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            if (sample.getPerson().getAffectedStatus() == AffectedStatus.AFFECTED) {
                affected.add(sample);
            } else if (sample.getPerson().getAffectedStatus() == AffectedStatus.UNAFFECTED) {
                unAffected.add(sample);
            } else {
                missing.add(sample);
            }
        }
        membersByStatus.put(AffectedStatus.AFFECTED, affected);
        membersByStatus.put(AffectedStatus.UNAFFECTED, unAffected);
        membersByStatus.put(AffectedStatus.MISSING, missing);
        return membersByStatus;
    }

    public static  MatchEnum merge(Set<MatchEnum> matches) {
        if (matches.contains(FALSE)) {
            return FALSE;
        } else if (matches.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }
}
