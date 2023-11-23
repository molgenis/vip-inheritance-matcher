package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Sample;

import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

public class XlrChecker extends XlChecker {

    protected Boolean checkSample(Sample sample, VariantContext variantContext) {
        Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
        if (genotype == null || !genotype.isCalled()) {
            return null;
        }

        switch (sample.getPerson().getAffectedStatus()) {
            case AFFECTED:
                switch (getSex(sample.getPerson().getSex(), genotype)) {
                    case MALE:
                        // Affected males have to be het. or hom. alt. (het is theoretically not possible in males, but can occur due to Pseudo Autosomal Regions).
                        if (hasVariant(genotype)) {
                            return true;
                        } else if (genotype.isMixed()) {
                            return null;
                        }
                    case FEMALE:
                        // Affected females have to be hom. alt.
                        if(genotype.isHomRef()){
                            return false;
                        }else if(hasVariant(genotype) && genotype.isMixed()){
                            return null;
                        }
                        return genotype.isHom();
                    default:
                        throw new IllegalArgumentException();
                }
            case UNAFFECTED:
                switch (getSex(sample.getPerson().getSex(), genotype)) {
                    case MALE:
                        // Healthy males cannot carry the variant.
                        if(hasVariant(genotype)){
                            return false;
                        }else if(genotype.isHomRef()){
                            return true;
                        }
                        return null;
                    case FEMALE:
                        // Healthy females cannot be hom. alt.
                        if(hasVariant(genotype) && genotype.isHom()){
                            return false;
                        }else if(hasVariant(genotype) && genotype.isMixed()){
                            return null;
                        }
                        return true;
                    default:
                        throw new IllegalArgumentException();
                }
            case MISSING:
                return null;
            default:
                throw new IllegalArgumentException();
        }
    }
}
