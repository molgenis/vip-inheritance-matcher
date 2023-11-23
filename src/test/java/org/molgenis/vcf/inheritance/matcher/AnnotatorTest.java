package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.Annotator.DENOVO;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MATCH;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MODES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.MATCHING_GENES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.POSSIBLE_COMPOUND;
import static org.molgenis.vcf.inheritance.matcher.checker.PedigreeTestUtil.createFamily;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.FALSE;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.TRUE;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.PedigreeInheritanceMatch;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sex;

@ExtendWith(MockitoExtension.class)
class AnnotatorTest {

  private Annotator annotator;

  @BeforeEach
  void setUp() {
    annotator = new Annotator();
  }

  @Test
  void annotateHeader() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    annotator.annotateHeader(vcfHeader);
    assertAll(
        () -> verify(vcfHeader)
            .addMetaDataLine(
                new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
                    VCFHeaderLineType.String,
                    "An enumeration of possible inheritance modes based on the pedigree of the sample. Potential values: AD, AD_IP, AR, AR_C, XLR, XLD")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
            VCFHeaderLineType.String,
            "Possible Compound hetrozygote variants.")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
            VCFHeaderLineType.Integer,
            "De novo variant.")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
            VCFHeaderLineType.Integer,
            "Inheritance Match: Genotypes, affected statuses and known gene inheritance patterns match.")),
        () -> verify(vcfHeader)
            .addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
                VCFHeaderLineType.String,
                "Genes with a (potential) inheritance match.")),
        () -> verify(vcfHeader, times(2)).getInfoHeaderLines(),
        () -> verify(vcfHeader, times(2)).getFormatHeaderLines(),
        () -> verify(vcfHeader, times(2)).getFilterLines(),
        () -> verify(vcfHeader).getContigLines(),
        () -> verify(vcfHeader).getOtherHeaderLines(),
        () -> verify(vcfHeader).getGenotypeSamples());

        verifyNoMoreInteractions(vcfHeader);
  }

  @Test
  void annotateHeaderEscapedQuotesFormat1() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.getFormatHeaderLines()).thenReturn(
        Set.of(new VCFFormatHeaderLine("TEST", VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "\"test\"")));
    VCFHeader newHeader = annotator.annotateHeader(vcfHeader);
    assertEquals(new VCFFormatHeaderLine("TEST", VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "\\\"test\"").toString(), newHeader.getFormatHeaderLine("TEST").toString());
  }

  @Test
  void annotateHeaderEscapedQuotesFormat2() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.getFormatHeaderLines()).thenReturn(
        Set.of(new VCFFormatHeaderLine("TEST", 1,
            VCFHeaderLineType.String,
            "\"test\"")));
    VCFHeader newHeader = annotator.annotateHeader(vcfHeader);
    assertEquals(new VCFFormatHeaderLine("TEST", 1,
        VCFHeaderLineType.String,
        "\\\"test\"").toString(), newHeader.getFormatHeaderLine("TEST").toString());
  }

  @Test
  void annotateHeaderEscapedQuotesInfo1() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.getInfoHeaderLines()).thenReturn(
        Set.of(new VCFInfoHeaderLine("TEST", VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "\"test\"")));
    VCFHeader newHeader = annotator.annotateHeader(vcfHeader);
    assertEquals(new VCFInfoHeaderLine("TEST", VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "\\\"test\"").toString(), newHeader.getInfoHeaderLine("TEST").toString());
  }

  @Test
  void annotateHeaderEscapedQuotesInfo2() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfHeader.getInfoHeaderLines()).thenReturn(
        Set.of(new VCFInfoHeaderLine("TEST", 1,
            VCFHeaderLineType.String,
            "\"test\"")));
    VCFHeader newHeader = annotator.annotateHeader(vcfHeader);
    assertEquals(new VCFInfoHeaderLine("TEST", 1,
        VCFHeaderLineType.String,
        "\\\"test\"").toString(), newHeader.getInfoHeaderLine("TEST").toString());
  }

  @Test
  void annotateInheritance() {
    Genotype genotype = createGenotype("Patient", "1/1");
    VariantContext vc = new VariantContextBuilder().chr("1").start(1).stop(1).alleles("T", "A")
        .genotypes(genotype)
        .make();
    Pedigree familyMap = createFamily(Sex.MALE, AffectedStatus.AFFECTED, AffectedStatus.UNAFFECTED,
        AffectedStatus.UNAFFECTED, "FAM");
    Map<String, Pedigree> families = Map.of("FAM", familyMap);

    Inheritance inheritance = Inheritance.builder().match(TRUE).denovo(true).pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AD_IP, false), new PedigreeInheritanceMatch(AR_C, false)))
        .compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation = Annotation.builder().inheritance(inheritance).matchingGenes(
        Set.of("GENE1", "GENE2")).build();
    Map<String, Annotation> annotationMap = Map.of("Patient", annotation);

    VariantContext actual = annotator.annotateInheritance(vc, families, annotationMap);

    assertAll(
        () -> assertEquals("AD_IP,AR_C",
            actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MODES)),
        () -> assertEquals("1",
            actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MATCH)),
        () -> assertEquals("GENE1,GENE2",
            actual.getGenotype("Patient").getExtendedAttribute(MATCHING_GENES)),
        () -> assertEquals("1", actual.getGenotype("Patient").getExtendedAttribute(DENOVO)),
        () -> assertEquals("OTHER_VARIANT",
            actual.getGenotype("Patient").getExtendedAttribute(POSSIBLE_COMPOUND))
    );
  }

  @Test
  void annotateInheritanceMissingGeneInheritancePattern() {
    Genotype genotype = createGenotype("Patient", "1/1");
    VariantContext vc = new VariantContextBuilder().chr("1").start(1).stop(1).alleles("T", "A")
            .genotypes(genotype)
            .make();
    Pedigree familyMap = createFamily(Sex.MALE, AffectedStatus.AFFECTED, AffectedStatus.UNAFFECTED,
            AffectedStatus.UNAFFECTED, "FAM");
    Map<String, Pedigree> families = Map.of("FAM", familyMap);

    Inheritance inheritance = Inheritance.builder().denovo(true)
            .pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP, false), new PedigreeInheritanceMatch(AR_C, false)))
            .compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation = Annotation.builder().inheritance(inheritance).build();
    Map<String, Annotation> annotationMap = Map.of("Patient", annotation);

    VariantContext actual = annotator.annotateInheritance(vc, families, annotationMap);

    assertAll(
            () -> assertEquals("AD_IP,AR_C",
                    actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MODES)),
            () -> assertNull(actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MATCH)),
            () -> assertNull(actual.getGenotype("Patient").getExtendedAttribute(MATCHING_GENES)),
            () -> assertEquals("1", actual.getGenotype("Patient").getExtendedAttribute(DENOVO)),
            () -> assertEquals("OTHER_VARIANT",
                    actual.getGenotype("Patient").getExtendedAttribute(POSSIBLE_COMPOUND))
    );
  }

  @Test
  void annotateInheritanceNoMatch() {
    Genotype genotype = createGenotype("Patient", "1/1");
    VariantContext vc = new VariantContextBuilder().chr("1").start(1).stop(1).alleles("T", "A")
            .genotypes(genotype)
            .make();
    Pedigree familyMap = createFamily(Sex.MALE, AffectedStatus.AFFECTED, AffectedStatus.UNAFFECTED,
            AffectedStatus.UNAFFECTED, "FAM");
    Map<String, Pedigree> families = Map.of("FAM", familyMap);

    Inheritance inheritance = Inheritance.builder().match(FALSE).denovo(true).pedigreeInheritanceMatches(
                    emptySet())
            .compounds(emptySet()).build();
    Annotation annotation = Annotation.builder().inheritance(inheritance).build();
    Map<String, Annotation> annotationMap = Map.of("Patient", annotation);

    VariantContext actual = annotator.annotateInheritance(vc, families, annotationMap);

    assertAll(
            () -> assertNull(actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MODES)),
            () -> assertEquals("0", actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MATCH)),
            () -> assertNull(actual.getGenotype("Patient").getExtendedAttribute(MATCHING_GENES)),
            () -> assertEquals("1", actual.getGenotype("Patient").getExtendedAttribute(DENOVO)),
            () -> assertNull(actual.getGenotype("Patient").getExtendedAttribute(POSSIBLE_COMPOUND))
    );
  }
}