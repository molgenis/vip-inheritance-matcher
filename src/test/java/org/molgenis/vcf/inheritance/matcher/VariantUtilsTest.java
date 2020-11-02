package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

@ExtendWith(MockitoExtension.class)
class VariantUtilsTest {

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
  }

  @ParameterizedTest
  @MethodSource("argumentProviderAuto")
  void isMendelianViolationAuto(List<Allele> probandGeno, List<Allele> fatherGeno,
      List<Allele> motherGeno, Boolean expected) {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("1");

    Genotype genotypeFather = mock(Genotype.class);
    when(genotypeFather.getAlleles()).thenReturn(fatherGeno);
    doReturn(genotypeFather).when(variantContext).getGenotype("Jimmy");

    Genotype genotypeMother = mock(Genotype.class);
    when(genotypeMother.getAlleles()).thenReturn(motherGeno);
    doReturn(genotypeMother).when(variantContext).getGenotype("Jane");

    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(probandGeno);
    when(genotypeProband.getAllele(0)).thenReturn(probandGeno.get(0));
    when(genotypeProband.getAllele(1)).thenReturn(probandGeno.get(1));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertEquals(expected, VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @ParameterizedTest
  @MethodSource("argumentProviderX")
  void isMendelianViolationX(List<Allele> probandGeno,
      List<Allele> motherGeno, Boolean expected) {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("X");

    Genotype genotypeMother = mock(Genotype.class);
    when(genotypeMother.getAlleles()).thenReturn(motherGeno);
    doReturn(genotypeMother).when(variantContext).getGenotype("Jane");

    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(probandGeno);
    when(genotypeProband.getAllele(0)).thenReturn(probandGeno.get(0));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertEquals(expected, VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @ParameterizedTest
  @MethodSource("argumentProviderY")
  void isMendelianViolationY(List<Allele> probandGeno, List<Allele> fatherGeno, Boolean expected) {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("Y");

    Genotype genotypeFather = mock(Genotype.class);
    when(genotypeFather.getAlleles()).thenReturn(fatherGeno);
    doReturn(genotypeFather).when(variantContext).getGenotype("Jimmy");

    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(probandGeno);
    when(genotypeProband.getAllele(0)).thenReturn(probandGeno.get(0));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertEquals(expected, VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @ParameterizedTest
  @MethodSource("argumentProviderMT")
  void isMendelianViolationMT(List<Allele> probandGeno,
      List<Allele> motherGeno, Boolean expected) {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("MT");

    Genotype genotypeMother = mock(Genotype.class);
    when(genotypeMother.getAlleles()).thenReturn(motherGeno);
    doReturn(genotypeMother).when(variantContext).getGenotype("Jane");

    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(probandGeno);
    when(genotypeProband.getAllele(0)).thenReturn(probandGeno.get(0));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertEquals(expected, VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @Test
  void isMendelianViolationTriploid() {
    VariantContext variantContext = mock(VariantContext.class);
    Allele allele1 = mock(Allele.class);
    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(Arrays.asList(allele1, allele1, allele1));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertTrue(VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @ParameterizedTest
  @MethodSource("argumentProviderAuto")
  void isMendelianViolationMaleXDuploid(List<Allele> probandGeno, List<Allele> fatherGeno,
      List<Allele> motherGeno, Boolean expected) {
      VariantContext variantContext = mock(VariantContext.class);
      when(variantContext.getContig()).thenReturn("X");

      Genotype genotypeFather = mock(Genotype.class);
      when(genotypeFather.getAlleles()).thenReturn(fatherGeno);
      doReturn(genotypeFather).when(variantContext).getGenotype("Jimmy");

      Genotype genotypeMother = mock(Genotype.class);
      when(genotypeMother.getAlleles()).thenReturn(motherGeno);
      doReturn(genotypeMother).when(variantContext).getGenotype("Jane");

      Genotype genotypeProband = mock(Genotype.class);
      when(genotypeProband.getAlleles()).thenReturn(probandGeno);
      when(genotypeProband.getAllele(0)).thenReturn(probandGeno.get(0));
      when(genotypeProband.getAllele(1)).thenReturn(probandGeno.get(1));
      doReturn(genotypeProband).when(variantContext).getGenotype("John");

      assertEquals(expected, VariantUtils.isMendelianViolation(trio, variantContext));
    }

  @Test
  void isMendelianViolationDuploidY() {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("Y");
    Allele allele1 = mock(Allele.class);
    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(Arrays.asList(allele1, allele1));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertTrue(VariantUtils.isMendelianViolation(trio, variantContext));
  }

  @Test
  void isMendelianViolationDuploidMT() {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("MT");
    Allele allele1 = mock(Allele.class);
    Genotype genotypeProband = mock(Genotype.class);
    when(genotypeProband.getAlleles()).thenReturn(Arrays.asList(allele1, allele1));
    doReturn(genotypeProband).when(variantContext).getGenotype("John");

    assertTrue(VariantUtils.isMendelianViolation(trio, variantContext));
  }


  static Stream<Arguments> argumentProviderX() {
    Allele allele1 = mock(Allele.class);
    when(allele1.isReference()).thenReturn(true);
    Allele allele2 = mock(Allele.class);

    return Stream.of(
        arguments(Arrays.asList(allele1),
            Arrays.asList(allele1, allele1), false),
        arguments(Arrays.asList(allele2),
            Arrays.asList(allele1, allele2), false));
  }

  static Stream<Arguments> argumentProviderY() {
    Allele allele1 = mock(Allele.class);
    when(allele1.isReference()).thenReturn(true);
    Allele allele2 = mock(Allele.class);

    return Stream.of(
        arguments(Arrays.asList(allele1), Arrays.asList(allele1),
            false),
        arguments(Arrays.asList(allele2), Arrays.asList(allele1),
            true));
  }

  static Stream<Arguments> argumentProviderMT() {
    Allele allele1 = mock(Allele.class);
    when(allele1.isReference()).thenReturn(true);
    Allele allele2 = mock(Allele.class);

    return Stream.of(
        arguments(Arrays.asList(allele1),
            Arrays.asList(allele1, allele1), false),
        arguments(Arrays.asList(allele2),
            Arrays.asList(allele1, allele2), false));
  }

  static Stream<Arguments> argumentProviderAuto() {
    Allele allele1 = mock(Allele.class);
    when(allele1.isReference()).thenReturn(true);
    Allele allele2 = mock(Allele.class);

    return Stream.of(
        arguments(Arrays.asList(allele1, allele2), Arrays.asList(allele1, allele1),
            Arrays.asList(allele1, allele1), true),
        arguments(Arrays.asList(allele1, allele2), Arrays.asList(allele1, allele2),
            Arrays.asList(allele1, allele1), false),
        arguments(Arrays.asList(allele1, allele2), Arrays.asList(allele1, allele2),
            Arrays.asList(allele1, allele2), false),
        arguments(Arrays.asList(allele2, allele2), Arrays.asList(allele1, allele1),
            Arrays.asList(allele1, allele1), true),
        arguments(Arrays.asList(allele2, allele2), Arrays.asList(allele1, allele1),
            Arrays.asList(allele1, allele2), true),
        arguments(Arrays.asList(allele2, allele2), Arrays.asList(allele1, allele2),
            Arrays.asList(allele1, allele2), false),
        arguments(Arrays.asList(allele2, allele2), Arrays.asList(allele2, allele2),
            Arrays.asList(allele1, allele2), false)

    );
  }


}