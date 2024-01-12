package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeMt;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class MtChecker extends HaploidChecker {
    public MatchEnum check(VariantContext variantContext, Pedigree family) {
        if (!onChromosomeMt(variantContext)) {
            return FALSE;
        }
        return checkFamily(variantContext, family);
    }
}
