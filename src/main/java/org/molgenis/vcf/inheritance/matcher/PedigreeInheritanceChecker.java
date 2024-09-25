package org.molgenis.vcf.inheritance.matcher;

import org.molgenis.vcf.inheritance.matcher.checker.*;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.utils.UnexpectedEnumException;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.springframework.stereotype.Component;

@Component
public class PedigreeInheritanceChecker {
    private final XldChecker xldChecker;
    private final XlrChecker xlrChecker;

    private final AdChecker adChecker;
    private final AdNonPenetranceChecker adNonPenetranceChecker;
    private final ArChecker arChecker;
    private final MtChecker mtChecker;
    private final YlChecker ylChecker;

    public PedigreeInheritanceChecker(XldChecker xldChecker, XlrChecker xlrChecker, AdChecker adChecker, AdNonPenetranceChecker adNonPenetranceChecker, ArChecker arChecker, YlChecker ylChecker, MtChecker mtChecker) {
        this.xldChecker = xldChecker;
        this.xlrChecker = xlrChecker;
        this.adChecker = adChecker;
        this.adNonPenetranceChecker = adNonPenetranceChecker;
        this.arChecker = arChecker;
        this.mtChecker = mtChecker;
        this.ylChecker = ylChecker;
    }

    MatchEnum check(VcfRecord vcfRecord, Pedigree pedigree, InheritanceMode mode) {
        MatchEnum result;
        switch(mode){
            case AD -> result = adChecker.check(vcfRecord, pedigree);
            case AD_IP -> result = adNonPenetranceChecker.check(vcfRecord, pedigree);
            case AR -> result = arChecker.check(vcfRecord, pedigree);
            case XLR -> result = xlrChecker.check(vcfRecord, pedigree);
            case XLD -> result = xldChecker.check(vcfRecord, pedigree);
            case MT -> result = mtChecker.check(vcfRecord, pedigree);
            case YL -> result = ylChecker.check(vcfRecord, pedigree);
            default -> throw new UnexpectedEnumException(mode);
        }
        return result;
    }
}
