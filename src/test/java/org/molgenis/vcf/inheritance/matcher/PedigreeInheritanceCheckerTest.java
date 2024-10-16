package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.checker.*;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.utils.sample.model.Pedigree;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

@ExtendWith(MockitoExtension.class)
class PedigreeInheritanceCheckerTest {

    @Mock
    XldChecker xldChecker;
    @Mock
    XlrChecker xlrChecker;
    @Mock
    AdChecker adChecker;
    @Mock
    AdNonPenetranceChecker adNonPenetranceChecker;
    @Mock
    ArChecker arChecker;
    @Mock
    YlChecker ylChecker;
    @Mock
    MtChecker mtChecker;

    private PedigreeInheritanceChecker pedigreeInheritanceChecker;

    @BeforeEach
    void setUp() {
        pedigreeInheritanceChecker = new PedigreeInheritanceChecker(xldChecker, xlrChecker, adChecker, adNonPenetranceChecker, arChecker, ylChecker, mtChecker);
    }

    @Test
    void testAdPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(adChecker.check(vcfRecord, family)).thenReturn(POTENTIAL);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.AD);
        assertEquals(POTENTIAL, actual);
    }

    @Test
    void testAdIpTrue() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(adNonPenetranceChecker.check(vcfRecord, family)).thenReturn(TRUE);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.AD_IP);
        assertEquals(TRUE, actual);
    }

    @Test
    void testArPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(arChecker.check(vcfRecord, family)).thenReturn(POTENTIAL);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.AR);
        assertEquals(POTENTIAL, actual);
    }

    @Test
    void testXldFalse() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(xldChecker.check(vcfRecord, family)).thenReturn(FALSE);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.XLD);
        assertEquals(FALSE, actual);
    }

    @Test
    void testXlrPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(xlrChecker.check(vcfRecord, family)).thenReturn(POTENTIAL);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.XLR);
        assertEquals(POTENTIAL, actual);
    }

    @Test
    void testYlTrue() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(ylChecker.check(vcfRecord, family)).thenReturn(TRUE);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.YL);
        assertEquals(TRUE, actual);
    }

    @Test
    void testMtPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord vcfRecord = new VcfRecord(vc, Set.of(Allele.ALT_A, Allele.ALT_G), Set.of(new GeneInfo("","",Set.of(InheritanceMode.AD_IP))));
        Pedigree family = mock(Pedigree.class);
        when(mtChecker.check(vcfRecord, family)).thenReturn(POTENTIAL);
        MatchEnum actual = pedigreeInheritanceChecker.check(vcfRecord, family, InheritanceMode.MT);
        assertEquals(POTENTIAL, actual);
    }
}