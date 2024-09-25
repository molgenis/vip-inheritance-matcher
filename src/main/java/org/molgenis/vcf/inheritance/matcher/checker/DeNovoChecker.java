package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils.*;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.ContigUtils;
import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeNovoChecker {

    //use original GT instead of inheritance matcher specific one since we are looking for "new alleles" not specifically pathogenic ones
    public MatchEnum checkDeNovo(VcfRecord vcfRecord, Sample proband) {
        Genotype probandGt = vcfRecord.getGenotype(proband.getPerson().getIndividualId());
        Genotype fatherGt = vcfRecord.getGenotype(proband.getPerson().getPaternalId());
        Genotype motherGt = vcfRecord.getGenotype(proband.getPerson().getMaternalId());

        if (!hasParents(proband)) {
            return POTENTIAL;
        }

        String contigId = vcfRecord.getContig();
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

    private static MatchEnum checkMaleXVariant(Genotype probandGt, Genotype motherGt) {
        if (probandGt == null || probandGt.isNoCall()) {
            return POTENTIAL;
        } else if (probandGt.hasAltAllele()) {
            if (motherGt != null && hasSameAltAlleles(probandGt, motherGt)) {
                return FALSE;
            } else if (motherGt != null && motherGt.isHomRef()) {
                return TRUE;
            } else {
                return POTENTIAL;
            }
        }
        return probandGt.isNoCall() ? POTENTIAL : FALSE;
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
            if (hasSameAltAlleles(probandGt, fatherGt)) {
                return FALSE;
            } else {
                return (!hasSameAltAlleles(probandGt, fatherGt) && !hasMissing(fatherGt)) ? TRUE : POTENTIAL;
            }
        } else if (hasMissing(probandGt)) {
            return fatherGt.hasAltAllele() ? FALSE : POTENTIAL;
        }
        return FALSE;
    }

    private static boolean hasSameAltAlleles(Genotype probandGt, Genotype fatherGt) {
        return probandGt.getAlleles().stream().filter(allele -> allele.isCalled() &&
                allele.isNonReference()).allMatch(allele -> fatherGt.getAlleles().contains(allele));
    }

    private static MatchEnum checkMtVariant(Genotype probandGt, Genotype motherGt) {
        if (probandGt == null || probandGt.isNoCall()) {
            return POTENTIAL;
        } else if (probandGt.hasAltAllele()) {
            if (motherGt != null && hasSameAltAlleles(probandGt, motherGt) && !motherGt.hasReference()) {
                return FALSE;
            } else if (motherGt != null && !hasSameAltAlleles(probandGt, motherGt) && motherGt.isCalled() && !motherGt.isMixed()) {
                return TRUE;
            } else {
                return POTENTIAL;
            }
        } else if (probandGt.hasMissingAllele()) {
            if (probandGt.hasAltAllele() && hasSameAltAlleles(probandGt, motherGt)) {
                return FALSE;
            } else {
                return POTENTIAL;
            }
        }
        return FALSE;
    }

    private MatchEnum checkRegular(Genotype probandGt, Genotype fatherGt, Genotype motherGt) {
        Set<MatchEnum> result = new HashSet<>();
        if (probandGt == null || probandGt.isNoCall()) {
            return POTENTIAL;
        }
        List<Allele> alleles = probandGt.getAlleles();
        if (alleles.size() != 2) {
            throw new UnsupportedOperationException("Checking 'regular' denovo calles requires diploid genotypes.");
        }
        Allele allele1 = alleles.get(0);
        Allele allele2 = alleles.get(1);
        result.add(checkAlleles(fatherGt, motherGt, allele1, allele2));

        return merge(result);
    }

    private static MatchEnum checkAlleles(Genotype fatherGt, Genotype motherGt, Allele allele1, Allele allele2) {
        if(allele1.isNoCall() || allele2.isNoCall()) {
            return checkPartialCalls(fatherGt, motherGt, allele1, allele2);
        }
        else if ((containsAlleleOrRef(motherGt, allele1) && containsAlleleOrRef(fatherGt, allele2)) ||
                (containsAlleleOrRef(motherGt, allele2) && containsAlleleOrRef(fatherGt, allele1))) {
            return FALSE;
        } else if (containsAlleleOrNoCall(motherGt, allele1) && (containsAlleleOrNoCall(fatherGt, allele2)) ||
                containsAlleleOrNoCall(motherGt, allele2) && (containsAlleleOrNoCall(fatherGt, allele1))) {
            return POTENTIAL;
        }
        return TRUE;
    }

    private static MatchEnum checkPartialCalls(Genotype fatherGt, Genotype motherGt, Allele allele1, Allele allele2) {
        if (allele1.isNoCall() && allele2.isCalled()) {
            if (containsAlleleOrNoCall(motherGt, allele2) || containsAlleleOrNoCall(fatherGt, allele2)) {
                return POTENTIAL;
            } else {
                return TRUE;
            }
        } else if (allele2.isNoCall() && allele1.isCalled()) {
            if (containsAlleleOrNoCall(motherGt, allele1) || containsAlleleOrNoCall(fatherGt, allele1)) {
                return POTENTIAL;
            } else {
                return TRUE;
            }
        }
        return FALSE;
    }

    private static boolean containsAlleleOrRef(Genotype genotype, Allele allele) {
        return allele.isReference() || genotype.getAlleles().contains(allele);
    }

    private static boolean containsAlleleOrNoCall(Genotype genotype, Allele allele1) {
        return containsAlleleOrRef(genotype, allele1) || containsAlleleOrRef(genotype, Allele.NO_CALL);
    }
}
