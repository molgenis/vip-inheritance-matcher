package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasParents;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;

public class DeNovoChecker {

    public Boolean checkDeNovo(VariantContext variantContext, Sample proband) {
        Genotype probandGt = variantContext.getGenotype(proband.getPerson().getIndividualId());
        Genotype fatherGt = variantContext.getGenotype(proband.getPerson().getPaternalId());
        Genotype motherGt = variantContext.getGenotype(proband.getPerson().getMaternalId());

        if (!hasParents(proband)) {
            return null;
        }
        if (onChromosomeX(variantContext) && proband.getPerson().getSex() == Sex.MALE) {
            if (hasVariant(probandGt)) {
                if (hasVariant(motherGt)) {
                    return false;
                } else if (motherGt.isHomRef()) {
                    return true;
                } else {
                    return null;
                }
            }
            return probandGt.isNoCall() ? null : false;
        } else {
            return checkRegular(probandGt, fatherGt, motherGt);
        }
    }

    private Boolean checkRegular(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        Boolean result = false;
        if (probandGt != null) {
            if (probandGt.isHom()) {
                result = checkHomozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isHet()) {
                result = checkHetrozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isMixed()) {
                result = checkMixed(probandGt, fatherGt, motherGt);
            } else {
                result = (hasVariant(motherGt) && hasVariant(fatherGt)) ? false: null;
            }
        }
        return result;
    }

    private static Boolean checkMixed(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        Boolean result;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                result = true;
            } else if (hasVariant(motherGt) && hasVariant(fatherGt)) {
                result = false;
            } else {
                result = null;
            }
        } else {
            if (hasVariant(motherGt) || hasVariant(fatherGt)) {
                result = false;
            } else {
                result = null;
            }
        }
        return result;
    }

    private static Boolean checkHetrozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        Boolean result = false;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                result = true;
            } else if (hasVariant(motherGt) || hasVariant(fatherGt)) {
                result = false;
            } else {
                result = null;
            }
        }
        return result;
    }

    private static Boolean checkHomozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        Boolean result = false;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() || fatherGt.isHomRef()) {
                result = true;
            } else if (hasVariant(motherGt) && hasVariant(fatherGt)) {
                result = false;
            } else {
                result = null;
            }
        }
        return result;
    }
}
