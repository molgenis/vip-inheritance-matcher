package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.molgenis.vcf.inheritance.matcher.Annotator.DENOVO;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MATCH;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MODES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.MATCHING_GENES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.POSSIBLE_COMPOUND;
import static org.molgenis.vcf.inheritance.matcher.Annotator.SUBINHERITANCE_MODES;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.matcher.util.PedigreeTestUtil.createFamily;
import static org.molgenis.vcf.inheritance.matcher.util.VariantContextTestUtil.createGenotype;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

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
                    "An enumeration of possible inheritance modes.")),
        () -> verify(vcfHeader)
            .addMetaDataLine(
                new VCFFormatHeaderLine(SUBINHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
                    VCFHeaderLineType.String,
                    "An enumeration of possible sub inheritance modes like e.g. compound, non penetrance.")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
            VCFHeaderLineType.String,
            "Possible Compound hetrozygote variants.")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
            VCFHeaderLineType.Integer,
            "Inheritance Denovo status.")),
        () -> verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
            VCFHeaderLineType.Integer,
            "Inheritance Match status.")),
        () -> verify(vcfHeader)
            .addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
                VCFHeaderLineType.String,
                "Genes with an inheritance match.")));

    verifyNoMoreInteractions(vcfHeader);
  }

  @Test
  void annotateInheritance() {
    Genotype genotype = createGenotype("Patient", "1/1");
    VariantContext vc = new VariantContextBuilder().chr("1").start(1).stop(1).alleles("T", "A").genotypes(genotype)
        .make();
    Map<String, Sample> familyMap = createFamily(Sex.MALE, AffectedStatus.AFFECTED, AffectedStatus.UNAFFECTED, AffectedStatus.UNAFFECTED, "FAM");
    Map<String, Map<String, Sample>> families = Map.of("FAM", familyMap);

    Inheritance inheritance = Inheritance.builder().denovo(true).inheritanceModes(
        Set.of(AD,AR)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP, SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation = Annotation.builder().inheritance(inheritance).matchingGenes(
        Set.of("GENE1","GENE2")).build();
    Map<String, Annotation> annotationMap = Map.of("Patient", annotation);

    VariantContext actual = annotator.annotateInheritance(vc, families, annotationMap);

    assertAll(
        () -> assertEquals("AR,AD", actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MODES)),
        () -> assertEquals("1", actual.getGenotype("Patient").getExtendedAttribute(INHERITANCE_MATCH)),
        () -> assertEquals("GENE1,GENE2", actual.getGenotype("Patient").getExtendedAttribute(MATCHING_GENES)),
        () -> assertEquals("1", actual.getGenotype("Patient").getExtendedAttribute(DENOVO)),
        () -> assertEquals("AD_IP,AR_C", actual.getGenotype("Patient").getExtendedAttribute(SUBINHERITANCE_MODES)),
        () -> assertEquals("OTHER_VARIANT", actual.getGenotype("Patient").getExtendedAttribute(POSSIBLE_COMPOUND))
    );
  }
}