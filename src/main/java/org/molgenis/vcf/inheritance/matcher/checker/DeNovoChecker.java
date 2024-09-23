package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.*;

import htsjdk.variant.variantcontext.Genotype;
import org.molgenis.vcf.inheritance.matcher.ContigUtils;
import org.molgenis.vcf.inheritance.matcher.VariantRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

@Component
public class DeNovoChecker {

    //use original GT instead of inheritance matcher specific one since we are looking for "new alleles" not specifically pathogenic ones
    public MatchEnum checkDeNovo(VariantRecord variantRecord, Sample proband) {
        Genotype probandGt = variantRecord.getGenotype(proband.getPerson().getIndividualId());
        Genotype fatherGt = variantRecord.getGenotype(proband.getPerson().getPaternalId());
        Genotype motherGt = variantRecord.getGenotype(proband.getPerson().getMaternalId());

        if (!hasParents(proband)) {
            return POTENTIAL;
        }

        String contigId = variantRecord.getContig();
        if ((contigId != null && ContigUtils.isChromosomeX(contigId)) && proband.getPerson().getSex() == Sex.MALE) {
            if (probandGt.hasAltAllele()) {
                if (motherGt.hasAltAllele()) {
                    return FALSE;
                } else if (motherGt.isHomRef()) {
                    return TRUE;
                } else {
                    return POTENTIAL;
                }
            }
            return probandGt.isNoCall() ? POTENTIAL : FALSE;
        } else if ((contigId != null && ContigUtils.isChromosomeY(contigId))) {
            return checkYLinkedVariant(proband, probandGt, fatherGt);
        } else if ((contigId != null && ContigUtils.isChromosomeMt(contigId))) {
            return checkMtVariant(probandGt, motherGt);
        } else {
            return checkRegular(probandGt, fatherGt, motherGt);
        }
    }

    private static MatchEnum checkYLinkedVariant(Sample proband, Genotype probandGt, Genotype fatherGt) {
        return switch (proband.getPerson().getSex()) {
            case MALE -> checkYLinkedVariantMale(probandGt, fatherGt);
            case FEMALE -> FALSE;
            default -> (fatherGt.hasAltAllele() || !probandGt.hasAltAllele()) ? FALSE : POTENTIAL;
        };
    }

    private static MatchEnum checkYLinkedVariantMale(Genotype probandGt, Genotype fatherGt) {
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

    private static MatchEnum checkMtVariant(Genotype probandGt, Genotype motherGt) {
        if (probandGt.hasAltAllele()) {
            if (motherGt.hasAltAllele()) {
                return FALSE;
            } else if (!motherGt.hasAltAllele() && motherGt.isCalled() && !motherGt.isMixed()) {
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
                result = (motherGt.hasAltAllele() && fatherGt.hasAltAllele()) ? FALSE : POTENTIAL;
            }
        }
        return result;
    }

    private static MatchEnum checkMixed(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
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

    private static MatchEnum checkHetrozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
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

    private static MatchEnum checkHomozygote(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
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
