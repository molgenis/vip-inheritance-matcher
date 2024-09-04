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

import static java.util.Collections.*;
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
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(FALSE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(record, family, FALSE)).thenReturn(POTENTIAL);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(FALSE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD_IP, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testXlRDTrue() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(TRUE);
        when(xlrChecker.check(record, family)).thenReturn(TRUE);
        when(adChecker.check(record, family)).thenReturn(FALSE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(record, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(FALSE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.XLD, false),
                        new PedigreeInheritanceMatch(InheritanceMode.XLR, false))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testArAdMixed() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(TRUE);
        when(arChecker.check(record, family)).thenReturn(POTENTIAL);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(FALSE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD, false),
                        new PedigreeInheritanceMatch(InheritanceMode.AR, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testAdDenovoMixed() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(TRUE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(TRUE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(
                Set.of(new PedigreeInheritanceMatch(InheritanceMode.AD, false))).denovo(TRUE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testArPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        VariantContext vc2 = mock(VariantContext.class);
        Allele ref = mock(Allele.class);
        when(ref.getBaseString()).thenReturn("A");
        Allele alt = mock(Allele.class);
        when(alt.getBaseString()).thenReturn("T");
        when(vc2.getContig()).thenReturn("chr1");
        when(vc2.getStart()).thenReturn(123);
        when(vc2.getReference()).thenReturn(ref);
        when(vc2.getAlternateAlleles()).thenReturn(List.of(alt));
        VcfRecord record2 = new VcfRecord(vc2, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(FALSE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(record, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(FALSE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(List.of(CompoundCheckResult.builder().possibleCompound(record2).isCertain(false).build()));

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.AR_C, true))).compounds(Set.of("chr1_123_A_T")).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testMtPotential() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(FALSE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(record, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(record, family)).thenReturn(POTENTIAL);
        when(ylChecker.check(record, family)).thenReturn(FALSE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(FALSE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.MT, true))).denovo(FALSE).build();

        assertEquals(expected, actual);
    }

    @Test
    void testYl() {
        VariantContext vc = mock(VariantContext.class);
        VcfRecord record = new VcfRecord(vc, emptyList());
        Pedigree family = mock(Pedigree.class);
        Sample sample = mock(Sample.class);
        ArCompoundChecker arCompoundChecker = mock(ArCompoundChecker.class);
        when(xldChecker.check(record, family)).thenReturn(FALSE);
        when(xlrChecker.check(record, family)).thenReturn(FALSE);
        when(adChecker.check(record, family)).thenReturn(FALSE);
        when(arChecker.check(record, family)).thenReturn(FALSE);
        when(adNonPenetranceChecker.check(record, family, FALSE)).thenReturn(FALSE);
        when(mtChecker.check(record, family)).thenReturn(FALSE);
        when(ylChecker.check(record, family)).thenReturn(TRUE);
        when(deNovoChecker.checkDeNovo(record, sample)).thenReturn(TRUE);
        Map<String, List<VcfRecord>> geneMap = emptyMap();
        when(arCompoundChecker.check(geneMap, record, family, FALSE)).thenReturn(emptyList());

        Inheritance actual = pedigreeInheritanceChecker.calculatePedigreeInheritance(geneMap, record, sample, family, arCompoundChecker);
        Inheritance expected = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.YL, false))).denovo(TRUE).build();

        assertEquals(expected, actual);
    }
}