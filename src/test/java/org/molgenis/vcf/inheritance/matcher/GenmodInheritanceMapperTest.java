package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XLD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XLR;
import static org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode.COMP;
import static org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode.HOM;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

@ExtendWith(MockitoExtension.class)
class GenmodInheritanceMapperTest {

  private GenmodInheritanceMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new GenmodInheritanceMapper();
  }

  @ParameterizedTest
  @MethodSource
  void mapInheritance(String input, InheritanceModeEnum inheritanceModeEnum, SubInheritanceMode subInheritanceMode,boolean denovo) {
    VariantContext vc = mock(VariantContext.class);
    when(vc.hasAttribute("GeneticModels")).thenReturn(true);
    when(vc.getAttributeAsStringList("GeneticModels", "")).thenReturn(
        Arrays.asList(String.format("FAM001:%s",input)));

    Annotation ann1 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(inheritanceModeEnum).subInheritanceMode(
                subInheritanceMode).build())).denovo(denovo).build();
    Map<String, Annotation> expected = Map.of("FAM001", ann1);
    assertEquals(expected, mapper.mapInheritance(vc));
  }

  private static Stream<Arguments> mapInheritance() {
    return Stream.of(
        Arguments.of("AR_hom",AR,HOM,false),
        Arguments.of("AR_hom_dn",AR,HOM,true),
        Arguments.of("AR_comp",AR,COMP,false),
        Arguments.of("AR_comp_dn",AR,COMP,true),
        Arguments.of("AD",AD,null,false),
        Arguments.of("AD_dn",AD,null,true),
        Arguments.of("XD", XLD,null,false),
        Arguments.of("XD_dn", XLD,null,true),
        Arguments.of("XR", XLR,null,false),
        Arguments.of("XR_dn", XLR,null,true)
    );
  }

  @Test
  void mapInheritanceMultiFam() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.hasAttribute("GeneticModels")).thenReturn(true);
    when(vc.getAttributeAsStringList("GeneticModels", "")).thenReturn(
        Arrays.asList("FAM001:AD_dn|AR_comp_dn", "FAM002:AD|AR_hom", "FAM003:AD|AR_comp"));

    Annotation ann1 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                COMP).build())).denovo(true).build();
    Annotation ann2 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                HOM).build())).denovo(false).build();
    Annotation ann3 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                COMP).build())).denovo(false).build();
    Map<String, Annotation> expected = Map.of("FAM001", ann1, "FAM002", ann2, "FAM003", ann3);
    assertEquals(expected, mapper.mapInheritance(vc));
  }
}