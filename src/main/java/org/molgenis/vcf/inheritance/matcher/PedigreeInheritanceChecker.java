package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
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
    private final MtChecker mtChecker;
    private final YlChecker ylChecker;
    private final DeNovoChecker deNovoChecker;

    public PedigreeInheritanceChecker(XldChecker xldChecker, XlrChecker xlrChecker, AdChecker adChecker, AdNonPenetranceChecker adNonPenetranceChecker, ArChecker arChecker, YlChecker ylChecker, MtChecker mtChecker, DeNovoChecker deNovoChecker) {
        this.xldChecker = xldChecker;
        this.xlrChecker = xlrChecker;
        this.adChecker = adChecker;
        this.adNonPenetranceChecker = adNonPenetranceChecker;
        this.arChecker = arChecker;
        this.mtChecker = mtChecker;
        this.ylChecker = ylChecker;
        this.deNovoChecker = deNovoChecker;
    }

    //TODO replace geneVariantMap with context incl pathogenic classes
    Inheritance calculatePedigreeInheritance(
            Map<String, List<VcfRecord>> geneVariantMap, VcfRecord record, Sample sample, Pedigree filteredFamily, ArCompoundChecker arCompoundChecker) {
        Inheritance inheritance = Inheritance.builder().build();
        //FIXME
        checkAr(geneVariantMap, record, filteredFamily, inheritance, arCompoundChecker);
        checkAd(record, filteredFamily, inheritance);
        checkXl(record, filteredFamily, inheritance);
        checkMt(record, filteredFamily, inheritance);
        checkYl(record, filteredFamily, inheritance);
        inheritance.setDenovo(deNovoChecker.checkDeNovo(record, sample));
        return inheritance;
    }

    private void checkMt(VcfRecord vcfRecord, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isMt = mtChecker.check(vcfRecord, family);
        if (isMt != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.MT, isMt == POTENTIAL));
        }
    }

    private void checkYl(VcfRecord vcfRecord, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isYl = ylChecker.check(vcfRecord, family);
        if (isYl != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.YL, isYl == POTENTIAL));
        }
    }

    private void checkXl(VcfRecord vcfRecord, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isXld = xldChecker.check(vcfRecord, family);
        if (isXld != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.XLD, isXld == POTENTIAL));
        }
        MatchEnum isXlr = xlrChecker.check(vcfRecord, family);
        if (isXlr != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.XLR, isXlr == POTENTIAL));
        }
    }

    private void checkAd(VcfRecord vcfRecord, Pedigree family,
                         Inheritance inheritance) {
        MatchEnum isAd = adChecker.check(vcfRecord, family);
        if (isAd != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AD, isAd == POTENTIAL));
        } else {
            MatchEnum isAdNonPenetrance = adNonPenetranceChecker.check(vcfRecord, family, isAd);
            if (isAdNonPenetrance != FALSE) {
                inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AD_IP, isAdNonPenetrance == POTENTIAL));
            }
        }
    }

    private void checkAr(Map<String, List<VcfRecord>> geneVariantMap,
                         VcfRecord vcfRecord, Pedigree family,
                         Inheritance inheritance, ArCompoundChecker arCompoundChecker) {
        MatchEnum isAr = arChecker.check(vcfRecord, family);
        if (isAr != FALSE) {
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AR, isAr == POTENTIAL));
        }
        List<CompoundCheckResult> compounds = arCompoundChecker
                .check(geneVariantMap, vcfRecord, family, isAr);
        if (!compounds.isEmpty()) {
            boolean isCertain = compounds.stream().anyMatch(CompoundCheckResult::isCertain);
            inheritance.addInheritanceMode(new PedigreeInheritanceMatch(InheritanceMode.AR_C, !isCertain));
            inheritance.setCompounds(compounds.stream().map(compoundCheckResult -> createKey(compoundCheckResult.getPossibleCompound())).collect(
                    Collectors.toSet()));
        }
    }

    private String createKey(VcfRecord vcfRecord) {
        return String.format("%s_%s_%s_%s", vcfRecord.getContig(), vcfRecord.getStart(),
                vcfRecord.getReference().getBaseString(),
                vcfRecord.getAlternateAlleles().stream().map(Allele::getBaseString)
                        .collect(Collectors.joining("/")));
    }
}
