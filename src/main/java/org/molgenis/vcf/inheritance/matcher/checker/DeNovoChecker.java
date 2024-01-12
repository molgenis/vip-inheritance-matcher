package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.*;
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
        } else if (onChromosomeY(variantContext)) {
            return checkYLinkedVariant(proband, probandGt, fatherGt);
        } else if (onChromosomeMt(variantContext)) {
            return checkMtVariant(probandGt, motherGt);
        } else {
            return checkRegular(probandGt, fatherGt, motherGt);
        }
    }

    private static MatchEnum checkYLinkedVariant(Sample proband, Genotype probandGt, Genotype fatherGt) {
        if (proband.getPerson().getSex() == Sex.FEMALE) {
            return FALSE;
        } else if (proband.getPerson().getSex() == Sex.MALE) {
            if (hasVariant(probandGt)) {
                if (hasVariant(fatherGt)) {
                    return FALSE;
                } else {
                    return (!hasVariant(fatherGt) && fatherGt.isCalled() && !fatherGt.isMixed()) ? TRUE : POTENTIAL;
                }
            } else if (probandGt.isNoCall() || probandGt.isMixed()) {
                return hasVariant(fatherGt) ? FALSE : POTENTIAL;
            }
            return FALSE;
        } else {
            return (hasVariant(fatherGt) || !hasVariant(probandGt)) ? FALSE : POTENTIAL;
        }
    }

    private static MatchEnum checkMtVariant(Genotype probandGt, Genotype motherGt) {
        if (hasVariant(probandGt)) {
            if (hasVariant(motherGt)) {
                return FALSE;
            } else if (!hasVariant(motherGt) && motherGt.isCalled() && !motherGt.isMixed()) {
                return TRUE;
            } else {
                return POTENTIAL;
            }
        } else if (probandGt.isNoCall() || probandGt.isMixed()) {
            if (hasVariant(motherGt)) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
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
                result = (hasVariant(motherGt) && hasVariant(fatherGt)) ? FALSE : POTENTIAL;
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
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                return TRUE;
            } else if (hasVariant(motherGt) || hasVariant(fatherGt)) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }

    private static MatchEnum checkHomozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        if (hasVariant(probandGt)) {
            if (motherGt.isHomRef() || fatherGt.isHomRef()) {
                return TRUE;
            } else if (hasVariant(motherGt) && hasVariant(fatherGt)) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }
}
