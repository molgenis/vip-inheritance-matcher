package org.molgenis.vcf.inheritance.matcher;

import org.molgenis.vcf.inheritance.matcher.checker.*;
import org.molgenis.vcf.inheritance.matcher.model.*;
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

    MatchEnum check(VariantGeneRecord record, Pedigree pedigree, InheritanceMode mode) {
        MatchEnum result;
        switch(mode){
            case AD -> result = adChecker.check(record, pedigree);
            case AD_IP -> result = adNonPenetranceChecker.check(record, pedigree);
            case AR -> result = arChecker.check(record, pedigree);
            case XLR -> result = xlrChecker.check(record, pedigree);
            case XLD -> result = xldChecker.check(record, pedigree);
            case MT -> result = mtChecker.check(record, pedigree);
            case YL -> result = ylChecker.check(record, pedigree);
            default -> throw new UnexpectedEnumException(mode);
        }
        return result;
    }
}
