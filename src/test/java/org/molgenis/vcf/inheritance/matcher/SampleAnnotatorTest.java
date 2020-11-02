package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.SampleAnnotatorImpl.HEADER_VIP_INHERITANCE;
import static org.molgenis.vcf.inheritance.matcher.SampleAnnotatorImpl.INHERITANCE_MODE_KEY;
import static org.molgenis.vcf.inheritance.matcher.SampleAnnotatorImpl.MENDELIAN_VIOLATION_KEY;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

@ExtendWith(MockitoExtension.class)
class SampleAnnotatorTest {

  SampleAnnotator sampleAnnotator;
  private Trio trio;

  @BeforeEach
  void setUp() {
    Trio.TrioBuilder trioBuilder = Trio.builder();
    trioBuilder.family("FAM001");
    trioBuilder.proband(
        Sample.builder()
            .person(new Person("FAM001", "John", "Jimmy", "Jane", MALE, AffectedStatus.AFFECTED))
            .index(0)
            .build());
    trioBuilder.father(
        Sample.builder()
            .person(new Person("FAM001", "Jimmy", "0", "0", MALE, AffectedStatus.UNAFFECTED))
            .index(1)
            .build());
    trioBuilder.mother(
        Sample.builder()
            .person(new Person("FAM001", "Jane", "0", "0", FEMALE, AffectedStatus.UNAFFECTED))
            .index(2)
            .build());
    this.trio = trioBuilder.build();

    sampleAnnotator = new SampleAnnotatorImpl();
  }

  @Test
  void annotate() {

    Annotation annotation = new Annotation(Arrays.asList("TEST","TEST2"));
    annotation.setMendelianViolation("0");

    Map<Trio, Annotation> annotations = Map.of(trio, annotation);
    VariantContext variantContext = mock(VariantContext.class);
    Allele allele1 = mock(Allele.class);
    when(allele1.isReference()).thenReturn(true);
    Allele allele2 = mock(Allele.class);
    doReturn("1").when(variantContext).getContig();
    doReturn(Arrays.asList(allele1,allele2)).when(variantContext).getAlleles();
    doReturn(1).when(variantContext).getStart();
    doReturn(".").when(variantContext).getID();
    Genotype genotype = mock(Genotype.class);
    when(genotype.getSampleName()).thenReturn("John");
    when(genotype.getAlleles()).thenReturn(Arrays.asList(allele1, allele2));
    when(genotype.isPhased()).thenReturn(false);
    GenotypesContext genotypes = GenotypesContext.create(genotype);
    when(variantContext.getGenotypes()).thenReturn(genotypes);
    when(variantContext.getGenotype("John")).thenReturn(genotype);

    VariantContext actual = sampleAnnotator.annotate(variantContext, annotations);

    assertAll(
        () -> assertEquals(Arrays.asList("TEST","TEST2"), actual.getGenotype("John").getExtendedAttribute(INHERITANCE_MODE_KEY)),
        () -> assertEquals("0", actual.getGenotype("John").getExtendedAttribute(MENDELIAN_VIOLATION_KEY)));
  }

  @Test
  void addFormatMetadata() {
    VCFHeader fileHeader = mock(VCFHeader.class);
    sampleAnnotator.addMetadata(fileHeader, Arrays.asList(Path.of("path/to/myPed1"),Path.of("path/to/myPed2")));
    verify(fileHeader)
        .addMetaDataLine(new VCFHeaderLine(HEADER_VIP_INHERITANCE, "Inheritance annotations based on ped files: myPed1,myPed2"));
    verify(fileHeader)
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODE_KEY, 1, VCFHeaderLineType.String,
            "Inheritance mode and compound information for this sample in the format: GENE1|MODE|COMPOUND,GENE2|MODE|COMPOUND"));
    verify(fileHeader)
        .addMetaDataLine(
            new VCFFormatHeaderLine(MENDELIAN_VIOLATION_KEY, 1, VCFHeaderLineType.Integer,
                "Indication if the variant is a mendelian violation for this sample."));
  }
}