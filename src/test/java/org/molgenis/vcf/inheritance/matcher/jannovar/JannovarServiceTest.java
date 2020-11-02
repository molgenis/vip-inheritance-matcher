package org.molgenis.vcf.inheritance.matcher.jannovar;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import de.charite.compbio.jannovar.mendel.bridge.VariantContextMendelianAnnotator;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;
import org.molgenis.vcf.inheritance.matcher.model.Person;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

@ExtendWith(MockitoExtension.class)
class JannovarServiceTest {

  @Mock
  JannovarAnnotatorFactory jannovarAnnotatorFactory;
  @Mock
  VariantContextMendelianAnnotator variantContextMendelianAnnotator;
  private JannovarService jannovarService;
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
    when(jannovarAnnotatorFactory.create(trio)).thenReturn(variantContextMendelianAnnotator);
    jannovarService = new JannovarService(jannovarAnnotatorFactory);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("argumentProvider")
  void matchInheritance(SubModeOfInheritance subModeOfInheritance, InheritanceModeEnum inheritanceModeEnum, Boolean compound) {
    Map<String, List<VariantContext>> variantsPerGene = new HashMap<>();
    VariantContext variantContext = mock(VariantContext.class);
    doReturn("1").when(variantContext).getContig();
    doReturn(Arrays.asList("A*,T")).when(variantContext).getAlleles();
    doReturn(1).when(variantContext).getStart();
    List<VariantContext> variants = new ArrayList<>();
    variants.add(variantContext);
    variantsPerGene.put("GENE", variants);

    ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> jannovarResult1 = ImmutableMap
        .of(subModeOfInheritance, ImmutableList.of(variantContext));
    doReturn(jannovarResult1).when(variantContextMendelianAnnotator)
        .computeCompatibleInheritanceSubModes(variants);

    Set<InheritanceResults> actual = jannovarService
        .matchInheritance(variantsPerGene, trio);

    InheritanceResults expectedInheritance = InheritanceResults.builder().trio(trio).gene("GENE")
        .variantInheritanceResults(Map.of("1:1:[A*,T]",
            Set.of(InheritanceMode.builder().mode(inheritanceModeEnum).isCompound(compound).build())))
        .build();
    assertEquals(Set.of(expectedInheritance), actual);
  }

  @SneakyThrows
  @Test
  void matchInheritanceMultiGeneMultiVariant() {
    Map<String, List<VariantContext>> variantsPerGene = new HashMap<>();
    VariantContext variantContext1 = mock(VariantContext.class);
    doReturn("1").when(variantContext1).getContig();
    doReturn(Arrays.asList("A*,T")).when(variantContext1).getAlleles();
    doReturn(1).when(variantContext1).getStart();
    VariantContext variantContext2 = mock(VariantContext.class);
    doReturn("1").when(variantContext2).getContig();
    doReturn(Arrays.asList("A*,T")).when(variantContext2).getAlleles();
    doReturn(2).when(variantContext2).getStart();
    List<VariantContext> variants1 = new ArrayList<>();
    variants1.add(variantContext1);
    variants1.add(variantContext2);
    List<VariantContext> variants2 = new ArrayList<>();
    variants2.add(variantContext1);
    variantsPerGene.put("GENE", variants1);
    variantsPerGene.put("GENE2", variants2);

    ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> jannovarResult1 = ImmutableMap
        .of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, ImmutableList.of(variantContext1),
            SubModeOfInheritance.AUTOSOMAL_DOMINANT, ImmutableList.of(variantContext2));
    ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> jannovarResult2 = ImmutableMap
        .of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, ImmutableList.of(variantContext1));
    doReturn(jannovarResult1).when(variantContextMendelianAnnotator)
        .computeCompatibleInheritanceSubModes(variants1);
    doReturn(jannovarResult2).when(variantContextMendelianAnnotator)
        .computeCompatibleInheritanceSubModes(variants2);

    Set<InheritanceResults> actual = jannovarService
        .matchInheritance(variantsPerGene, trio);

    InheritanceResults expectedInheritance1 = InheritanceResults.builder().trio(trio).gene("GENE")
        .variantInheritanceResults(Map.of("1:1:[A*,T]",
            Set.of(InheritanceMode.builder().mode(InheritanceModeEnum.AR).isCompound(false).build()),
            "1:2:[A*,T]", Set.of(
                InheritanceMode.builder().mode(InheritanceModeEnum.AD).build())))
        .build();
    InheritanceResults expectedInheritance2 = InheritanceResults.builder().trio(trio).gene("GENE2")
        .variantInheritanceResults(Map.of("1:1:[A*,T]",
            Set.of(InheritanceMode.builder().mode(InheritanceModeEnum.AR).isCompound(true).build()))).build();
    Set<InheritanceResults> expected = Set.of(expectedInheritance1, expectedInheritance2);
    assertEquals(expected, actual);
  }

  @SneakyThrows
  @Test
  void matchInheritanceMultipleModes() {
    Map<String, List<VariantContext>> variantsPerGene = new HashMap<>();
    VariantContext variantContext1 = mock(VariantContext.class);
    doReturn("1").when(variantContext1).getContig();
    doReturn(Arrays.asList("A*,T")).when(variantContext1).getAlleles();
    doReturn(1).when(variantContext1).getStart();
    List<VariantContext> variants1 = new ArrayList<>();
    variants1.add(variantContext1);
    variantsPerGene.put("GENE", variants1);

    ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> jannovarResult1 = ImmutableMap
        .of(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, ImmutableList.of(variantContext1),
            SubModeOfInheritance.X_RECESSIVE_COMP_HET, ImmutableList.of(variantContext1));
    doReturn(jannovarResult1).when(variantContextMendelianAnnotator)
        .computeCompatibleInheritanceSubModes(variants1);

    Set<InheritanceResults> actual = jannovarService
        .matchInheritance(variantsPerGene, trio);

    InheritanceResults expectedInheritance1 = InheritanceResults.builder().trio(trio).gene("GENE")
        .variantInheritanceResults(Map.of("1:1:[A*,T]",
            Set.of(InheritanceMode.builder().mode(InheritanceModeEnum.XR).isCompound(false).build(),
                InheritanceMode.builder().mode(InheritanceModeEnum.XR).isCompound(true).build())))
        .build();
    Set<InheritanceResults> expected = Set.of(expectedInheritance1);
    assertEquals(expected, actual);
  }

  static Stream<Arguments> argumentProvider(){
    return Stream.of(
        arguments(SubModeOfInheritance.AUTOSOMAL_DOMINANT, InheritanceModeEnum.AD, null),
        arguments(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, InheritanceModeEnum.AR, true),
        arguments(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, InheritanceModeEnum.AR, false),
        arguments(SubModeOfInheritance.X_DOMINANT, InheritanceModeEnum.XD, null),
        arguments(SubModeOfInheritance.X_RECESSIVE_COMP_HET, InheritanceModeEnum.XR, true),
        arguments(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, InheritanceModeEnum.XR, false),
        arguments(SubModeOfInheritance.MITOCHONDRIAL, InheritanceModeEnum.MT, null)
    );
  }
}