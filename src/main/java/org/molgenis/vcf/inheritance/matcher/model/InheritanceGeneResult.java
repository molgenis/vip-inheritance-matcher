package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.molgenis.vcf.utils.UnexpectedEnumException;

import java.util.HashSet;
import java.util.Set;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Data
@Builder
public class InheritanceGeneResult implements Comparable<InheritanceGeneResult> {

    @NonNull
    GeneInfo geneInfo;
    @Builder.Default
    Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = new HashSet<>();
    @Builder.Default
    Set<CompoundCheckResult> compounds = new HashSet<>();

    public void addInheritanceMode(PedigreeInheritanceMatch pedigreeInheritanceMatch) {
        pedigreeInheritanceMatches.add(pedigreeInheritanceMatch);
    }

    /**
     * If there is a match between sample inheritance modes and gene inheritance modes:
     * - inheritance match is true
     * If there are no matches between sample inheritance modes and gene inheritance modes:
     *  - inheritance match is unknown ifa gene has unknown inheritance pattern.
     *  - inheritance match is false if a gene has known (but mismatching) inheritance pattern.
     */
    public MatchEnum getMatch() {
        //If no inheritance pattern is suitable for the sample, regardless of the gene: inheritance match is false.
        if(pedigreeInheritanceMatches.isEmpty()){
            return FALSE;
        }
        boolean containsUnknownGene = false;

        Set<InheritanceMode> geneInheritanceModes = geneInfo
                .inheritanceModes();
        if( geneInheritanceModes.isEmpty() ){
            containsUnknownGene = true;
        }
        if (geneInheritanceModes.stream()
                .anyMatch(geneInheritanceMode -> isMatch(pedigreeInheritanceMatches, geneInheritanceMode))) {
            if(pedigreeInheritanceMatches.stream().anyMatch(pedigreeInheritanceMatch -> !pedigreeInheritanceMatch.isUncertain())){
                return TRUE;
            }else {
                return POTENTIAL;
            }
        }
        return containsUnknownGene? POTENTIAL : FALSE;
    }
    private static Boolean isMatch(Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches, InheritanceMode geneInheritanceMode) {
        for(PedigreeInheritanceMatch pedigreeInheritanceMatch : pedigreeInheritanceMatches) {
            switch (pedigreeInheritanceMatch.inheritanceMode()) {
                case AD, AD_IP -> {
                    if (geneInheritanceMode == AD) {
                        return true;
                    }
                }
                case AR, AR_C -> {
                    if (geneInheritanceMode == AR) {
                        return true;
                    }
                }
                case XLR, XLD -> {
                    if (geneInheritanceMode == XL) {
                        return true;
                    }
                }
                case YL -> {
                    if (geneInheritanceMode == YL) {
                        return true;
                    }
                }
                case MT -> {
                    if (geneInheritanceMode == MT) {
                        return true;
                    }
                }
                default -> throw new UnexpectedEnumException(pedigreeInheritanceMatch.inheritanceMode());
            }
        }
        return false;
    }

    @Override
    public int compareTo(InheritanceGeneResult o) {
        return geneInfo.compareTo(o.getGeneInfo());
    }
}
