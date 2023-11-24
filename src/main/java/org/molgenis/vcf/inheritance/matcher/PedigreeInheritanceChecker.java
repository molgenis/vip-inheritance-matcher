package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.checker.*;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@Component
public class PedigreeInheritanceChecker {
    private final XldChecker xldChecker;
    private final XlrChecker xlrChecker;

    private final AdChecker adChecker;
    private final AdNonPenetranceChecker adNonPenetranceChecker;
    private final ArChecker arChecker;
    private final DeNovoChecker deNovoChecker;

    public PedigreeInheritanceChecker(XldChecker xldChecker, XlrChecker xlrChecker, AdChecker adChecker, AdNonPenetranceChecker adNonPenetranceChecker, ArChecker arChecker, DeNovoChecker deNovoChecker) {
        this.xldChecker = xldChecker;
        this.xlrChecker = xlrChecker;
        this.adChecker = adChecker;
        this.adNonPenetranceChecker = adNonPenetranceChecker;
        this.arChecker = arChecker;
        this.deNovoChecker = deNovoChecker;
    }

    Inheritance calculatePedigreeInheritance(
            Map<String, List<VariantContext>> geneVariantMap, VariantContext variantContext, Sample sample, Pedigree filteredFamily, ArCompoundChecker arCompoundChecker) {
        Inheritance inheritance = Inheritance.builder().build();
        checkAr(geneVariantMap, variantContext, filteredFamily, inheritance, arCompoundChecker);
        checkAd(variantContext, filteredFamily, inheritance);
        checkXl(variantContext, filteredFamily, inheritance);
        inheritance.setDenovo(deNovoChecker.checkDeNovo(variantContext, sample));
        return inheritance;
    }


    private void checkXl(VariantContext variantContext, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isXld = xldChecker.check(variantContext, family);
        if (isXld != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.XLD, isXld == POTENTIAL));
        }
        MatchEnum isXlr = xlrChecker.check(variantContext, family);
        if (isXlr != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.XLR, isXlr == POTENTIAL));
        }
    }

    private void checkAd(VariantContext variantContext, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isAd = adChecker.check(variantContext, family);
        if (isAd != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AD, isAd == POTENTIAL));
        } else {
            MatchEnum isAdNonPenetrance = adNonPenetranceChecker.check(variantContext, family, isAd);
            if (isAdNonPenetrance != FALSE) {
                inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AD_IP, isAdNonPenetrance == POTENTIAL));
            }
        }
    }

    private void checkAr(Map<String, List<VariantContext>> geneVariantMap,
                         VariantContext variantContext, Pedigree family,
                         Inheritance inheritance, ArCompoundChecker arCompoundChecker) {
        MatchEnum isAr = arChecker.check(variantContext, family);
        if (isAr != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AR, isAr == POTENTIAL));
        } else {
            List<CompoundCheckResult> compounds = arCompoundChecker
                    .check(geneVariantMap, variantContext, family);
            if (!compounds.isEmpty()) {
                boolean isCertain = compounds.stream().anyMatch(compoundCheckResult -> compoundCheckResult.isCertain());
                inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AR_C, !isCertain));
                inheritance.setCompounds(compounds.stream().map(compoundCheckResult -> createKey(compoundCheckResult.getPossibleCompound())).collect(
                        Collectors.toSet()));
            }
        }
    }

    private String createKey(VariantContext compound) {
        return String.format("%s_%s_%s_%s", compound.getContig(), compound.getStart(),
                compound.getReference().getBaseString(),
                compound.getAlternateAlleles().stream().map(Allele::getBaseString)
                        .collect(Collectors.joining("/")));
    }
}
