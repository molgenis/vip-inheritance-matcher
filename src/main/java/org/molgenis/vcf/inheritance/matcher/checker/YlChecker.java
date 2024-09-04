package org.molgenis.vcf.inheritance.matcher.checker;

import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeY;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class YlChecker extends HaploidChecker{
    public MatchEnum check(VcfRecord vcfRecord, Pedigree family) {
        if (!onChromosomeY(vcfRecord)) {
            return FALSE;
        }
        return checkFamily(vcfRecord, family);
    }

    @Override
    public MatchEnum checkFamily(VcfRecord vcfRecord, Pedigree family) {
        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            if (sample.getPerson().getSex() == Sex.FEMALE) {
                //female familty members do not play a role in Y-linked inheritance
                results.add(TRUE);
            } else {
                results.add(checkSample(sample, vcfRecord));
            }
        }
        if (results.contains(FALSE)) {
            return FALSE;
        } else if (results.contains(POTENTIAL)) {
            return POTENTIAL;
        }
        return TRUE;
    }
}
