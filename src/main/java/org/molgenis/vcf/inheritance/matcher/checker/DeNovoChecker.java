package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasParents;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.hasVariant;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

@Component
public class DeNovoChecker {

    public MatchEnum checkDeNovo(VariantContext variantContext, Sample proband) {
        Genotype probandGt = variantContext.getGenotype(proband.getPerson().getIndividualId());
        Genotype fatherGt = variantContext.getGenotype(proband.getPerson().getPaternalId());
        Genotype motherGt = variantContext.getGenotype(proband.getPerson().getMaternalId());

        if (!hasParents(proband)) {
            return POTENTIAL;
        }
        if (onChromosomeX(variantContext) && proband.getPerson().getSex() == Sex.MALE) {
            if (hasVariant(probandGt)) {
                if (hasVariant(motherGt)) {
                    return FALSE;
                } else if (motherGt.isHomRef()) {
                    return TRUE;
                } else {
                    return POTENTIAL;
                }
            }
            return probandGt.isNoCall() ? POTENTIAL : FALSE;
        } else {
            return checkRegular(probandGt, fatherGt, motherGt);
        }
    }

    private MatchEnum checkRegular(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        MatchEnum result = FALSE;
        if (probandGt != null) {
            if (probandGt.isHom()) {
                result = checkHomozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isHet()) {
                result = checkHetrozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isMixed()) {
                result = checkMixed(probandGt, fatherGt, motherGt);
            } else {
                result = (hasVariant(motherGt) && hasVariant(fatherGt)) ? FALSE: POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkMixed(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        MatchEnum result;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                result = TRUE;
            } else if (hasVariant(motherGt) && hasVariant(fatherGt)) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        } else {
            if (hasVariant(motherGt) || hasVariant(fatherGt)) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkHetrozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        MatchEnum result = FALSE;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                result = TRUE;
            } else if (hasVariant(motherGt) || hasVariant(fatherGt)) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkHomozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        MatchEnum result = FALSE;
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() || fatherGt.isHomRef()) {
                result = TRUE;
            } else if (hasVariant(motherGt) && hasVariant(fatherGt)) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        }
        return result;
    }
}
