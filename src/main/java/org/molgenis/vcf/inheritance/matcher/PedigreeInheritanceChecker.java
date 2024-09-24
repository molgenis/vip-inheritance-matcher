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

    MatchEnum check(VariantRecord variantRecord, Pedigree pedigree, InheritanceMode mode) {
        MatchEnum result;
        switch(mode){
            case AD -> result = adChecker.check(variantRecord, pedigree);
            case AD_IP -> result = adNonPenetranceChecker.check(variantRecord, pedigree);
            case AR -> result = arChecker.check(variantRecord, pedigree);
            case XLR -> result = xlrChecker.check(variantRecord, pedigree);
            case XLD -> result = xldChecker.check(variantRecord, pedigree);
            case MT -> result = mtChecker.check(variantRecord, pedigree);
            case YL -> result = ylChecker.check(variantRecord, pedigree);
            default -> throw new UnexpectedEnumException(mode);
        }
        return result;
    }
}
