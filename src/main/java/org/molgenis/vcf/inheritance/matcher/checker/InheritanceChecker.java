package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.HashSet;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

public abstract class InheritanceChecker {

    public MatchEnum checkFamily(VariantContext variantContext, Pedigree family) {
        Set<MatchEnum> results = new HashSet<>();
        for (Sample sample : family.getMembers().values()) {
            results.add(checkSample(sample, variantContext));
        }
        if(results.contains(FALSE)){
            return FALSE;
        }else if(results.contains(POTENTIAL)){
            return POTENTIAL;
        }
        return TRUE;
    }

    abstract MatchEnum checkSample(Sample sample, VariantContext variantContext);
}
