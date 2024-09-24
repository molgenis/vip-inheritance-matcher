package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.*;

import org.molgenis.vcf.inheritance.matcher.ContigUtils;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

@Component
public class DeNovoChecker {

    //use original GT instead of inheritance matcher specific one since we are looking for "new alleles" not specifically pathogenic ones
    public MatchEnum checkDeNovo(VariantRecord variantRecord, Sample proband) {
        EffectiveGenotype probandGt = variantRecord.getGenotype(proband.getPerson().getIndividualId());
        EffectiveGenotype fatherGt = variantRecord.getGenotype(proband.getPerson().getPaternalId());
        EffectiveGenotype motherGt = variantRecord.getGenotype(proband.getPerson().getMaternalId());

        if (!hasParents(proband)) {
            return POTENTIAL;
        }

        String contigId = variantRecord.getContig();
        if ((contigId != null && ContigUtils.isChromosomeX(contigId)) && proband.getPerson().getSex() == Sex.MALE) {
            return checkMaleXVariant(probandGt, motherGt);
        } else if ((contigId != null && ContigUtils.isChromosomeY(contigId))) {
            return checkYLinkedVariant(proband, probandGt, fatherGt);
        } else if ((contigId != null && ContigUtils.isChromosomeMt(contigId))) {
            return checkMtVariant(probandGt, motherGt);
        } else {
            return checkRegular(probandGt, fatherGt, motherGt);
        }
    }

    private static MatchEnum checkMaleXVariant(EffectiveGenotype probandGt, EffectiveGenotype motherGt) {
        if(probandGt == null){
            return POTENTIAL;
        } else if (probandGt.hasAltAllele()) {
            if (motherGt != null && motherGt.hasAltAllele()) {
                return FALSE;
            } else if (motherGt != null && motherGt.isHomRef()) {
                return TRUE;
            } else {
                return POTENTIAL;
            }
        }
        return probandGt.isNoCall() ? POTENTIAL : FALSE;
    }

    private static MatchEnum checkYLinkedVariant(Sample proband, EffectiveGenotype probandGt, EffectiveGenotype fatherGt) {
        return switch (proband.getPerson().getSex()) {
            case MALE -> checkYLinkedVariantMale(probandGt, fatherGt);
            case FEMALE -> FALSE;
            default -> (fatherGt.hasAltAllele() || !probandGt.hasAltAllele()) ? FALSE : POTENTIAL;
        };
    }

    private static MatchEnum checkYLinkedVariantMale(EffectiveGenotype probandGt, EffectiveGenotype fatherGt) {
        if (probandGt.hasAltAllele()) {
            if (fatherGt.hasAltAllele()) {
                return FALSE;
            } else {
                return (!fatherGt.hasAltAllele() && !hasMissing(fatherGt)) ? TRUE : POTENTIAL;
            }
        } else if (hasMissing(probandGt)) {
            return fatherGt.hasAltAllele() ? FALSE : POTENTIAL;
        }
        return FALSE;
    }

    private static MatchEnum checkMtVariant(EffectiveGenotype probandGt, EffectiveGenotype motherGt) {
        if (probandGt == null) {
            return POTENTIAL;
        }else if (probandGt.hasAltAllele()) {
            if (motherGt != null && motherGt.hasAltAllele()) {
                return FALSE;
            } else if (motherGt != null && !motherGt.hasAltAllele() && motherGt.isCalled() && !motherGt.isMixed()) {
                return TRUE;
            } else {
                return POTENTIAL;
            }
        } else if (probandGt.isNoCall() || probandGt.isMixed()) {
            if (motherGt.hasAltAllele()) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }

    private MatchEnum checkRegular(EffectiveGenotype probandGt, EffectiveGenotype fatherGt, EffectiveGenotype motherGt) {
        MatchEnum result = FALSE;
        if (probandGt != null) {
            if (probandGt.isHom()) {
                result = checkHomozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isHet()) {
                result = checkHetrozygote(probandGt, fatherGt, motherGt);
            } else if (probandGt.isMixed()) {
                result = checkMixed(probandGt, fatherGt, motherGt);
            } else {
                result = (motherGt.hasAltAllele() && fatherGt.hasAltAllele()) ? FALSE : POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkMixed(EffectiveGenotype probandGt, EffectiveGenotype fatherGt, EffectiveGenotype motherGt) {
        MatchEnum result;
        if (probandGt.hasAltAllele()) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                result = TRUE;
            } else if (motherGt.hasAltAllele() && fatherGt.hasAltAllele()) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        } else {
            if (motherGt.hasAltAllele() || fatherGt.hasAltAllele()) {
                result = FALSE;
            } else {
                result = POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkHetrozygote(EffectiveGenotype probandGt, EffectiveGenotype fatherGt, EffectiveGenotype motherGt) {
        if (probandGt.hasAltAllele()) {
            if (motherGt.isHomRef() && fatherGt.isHomRef()) {
                return TRUE;
            } else if (motherGt.hasAltAllele() || fatherGt.hasAltAllele()) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }

    private static MatchEnum checkHomozygote(EffectiveGenotype probandGt, EffectiveGenotype fatherGt, EffectiveGenotype motherGt) {
        if (probandGt.hasAltAllele()) {
            if (motherGt.isHomRef() || fatherGt.isHomRef()) {
                return TRUE;
            } else if (motherGt.hasAltAllele() && fatherGt.hasAltAllele()) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }
}
