package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XR;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

@ExtendWith(MockitoExtension.class)
class GenmodInheritanceMapperTest {

  GenmodInheritanceMapper mapper = new GenmodInheritanceMapper();

  @Test
  void mapInheritance() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.hasAttribute("GeneticModels")).thenReturn(true);
    when(vc.getAttributeAsStringList("GeneticModels", "")).thenReturn(
        Arrays.asList("FAM001:AD_dn|AR_comp_dn", "FAM002:AD|AR_hom", "FAM003:AD|AR_comp"));

    Annotation ann1 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                SubInheritanceMode.COMP).build())).denovo(true).build();
    Annotation ann2 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                SubInheritanceMode.HOM).build())).denovo(false).build();
    Annotation ann3 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(AD).build(),
            InheritanceMode.builder().inheritanceModeEnum(AR).subInheritanceMode(
                SubInheritanceMode.COMP).build())).denovo(false).build();
    Map<String,Annotation> expected = Map.of("FAM001", ann1, "FAM002", ann2, "FAM003", ann3);
    assertEquals(expected, mapper.mapInheritance(vc));
  }

  @Test
  void mapInheritanceX() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.hasAttribute("GeneticModels")).thenReturn(true);
    when(vc.getAttributeAsStringList("GeneticModels", ""))
        .thenReturn(Arrays.asList("FAM001:XD_dn|XR_dn", "FAM002:XD|XR", "FAM003:XD"));

    Annotation ann1 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(XD).build(),
            InheritanceMode.builder().inheritanceModeEnum(XR).build())).denovo(true).build();
    Annotation ann2 = Annotation.builder().inheritanceModes(
        Set.of(InheritanceMode.builder().inheritanceModeEnum(XD).build(),
            InheritanceMode.builder().inheritanceModeEnum(XR).build())).denovo(false).build();
    Annotation ann3 = Annotation.builder()
        .inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(XD).build()))
        .denovo(false).build();
    Map<String,Annotation> expected = Map.of("FAM001", ann1, "FAM002", ann2, "FAM003", ann3);
    assertEquals(expected, mapper.mapInheritance(vc));
  }
}