package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.checker.*;
import org.molgenis.vcf.inheritance.matcher.model.CompoundCheckResult;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.PedigreeInheritanceMatch;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
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
    @Mock
    DeNovoChecker deNovoChecker;
    private PedigreeInheritanceChecker pedigreeInheritanceChecker;

    @BeforeEach
    void setUp() {
        pedigreeInheritanceChecker = new PedigreeInheritanceChecker(xldChecker, xlrChecker, adChecker, adNonPenetranceChecker, arChecker, ylChecker, mtChecker, deNovoChecker);
    }

    @Test
    void testAdIpPotential() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(FALSE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(vc, family, FALSE)).thenReturn(POTENTIAL);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(FALSE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD_IP, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testXlRDTrue() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(TRUE);
        when(xlrChecker.check(vc, family)).thenReturn(TRUE);
        when(adChecker.check(vc, family)).thenReturn(FALSE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(vc, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(FALSE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.XLD, false),
                        new PedigreeInheritanceMatch(InheritanceMode.XLR, false))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testArAdMixed() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(TRUE);
        when(arChecker.check(vc, family)).thenReturn(POTENTIAL);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(FALSE);
        Map<String, List<VariantContext>> geneMap = emptyMap();

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD, false),
                        new PedigreeInheritanceMatch(InheritanceMode.AR, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testAdDenovoMixed() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(TRUE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(TRUE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD, false))).denovo(TRUE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testArPotential() {
        VariantContext vc = mock(VariantContext.class);
        VariantContext vc2 = mock(VariantContext.class);
        Allele ref = mock(Allele.class);
        when(ref.getBaseString()).thenReturn("A");
        Allele alt = mock(Allele.class);
        when(alt.getBaseString()).thenReturn("T");
        when(vc2.getContig()).thenReturn("chr1");
        when(vc2.getStart()).thenReturn(123);
        when(vc2.getReference()).thenReturn(ref);
        when(vc2.getAlternateAlleles()).thenReturn(List.of(alt));
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(FALSE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(vc, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(FALSE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(List.of(CompoundCheckResult.builder().possibleCompound(vc2).isCertain(false).build()));

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.AR_C, true))).compounds(Set.of("chr1_123_A_T")).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testMtPotential() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(FALSE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(vc, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(vc, family)).thenReturn(POTENTIAL);
        when(ylChecker.check(vc, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(FALSE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.MT, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testYl() {
        VariantContext vc = mock(VariantContext.class);
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(vc, family)).thenReturn(FALSE);
        when(xlrChecker.check(vc, family)).thenReturn(FALSE);
        when(adChecker.check(vc, family)).thenReturn(FALSE);
        when(arChecker.check(vc, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(vc, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(vc, family)).thenReturn(FALSE);
        when(ylChecker.check(vc, family)).thenReturn(TRUE);
        when(deNovoChecker.checkDeNovo(vc, sample)).thenReturn(TRUE);
        Map<String, List<VariantContext>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, vc, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, vc, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.YL, false))).denovo(TRUE).build();

        assertEquals(expected, actual);
    }
}