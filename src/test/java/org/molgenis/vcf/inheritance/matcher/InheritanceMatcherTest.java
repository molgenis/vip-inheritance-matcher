package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

@ExtendWith(MockitoExtension.class)
class InheritanceMatcherTest {

  @Mock
  Annotator annotator;
  @Mock
  GenmodInheritanceMapper genmodInheritanceMapper;
  @Mock
  GenmodCompoundMapper genmodCompoundMapper;
  private InheritanceMatcher inheritanceMatcher;

  @BeforeEach
  void setUp() {
    inheritanceMatcher = new InheritanceMatcher(annotator, genmodInheritanceMapper,
        genmodCompoundMapper);
  }

  @Test
  void matchInheritanceAD() {
    VariantContext vc = mock(VariantContext.class);
    Map<String, Annotation> annotations = Map.of("FAM",
        Annotation.builder()
            .inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AD).build())).build());
    VepMapper vepMapper = mock(VepMapper.class);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = new HashMap<>();
    geneInheritanceMap.put("GENE1", Set.of(InheritanceModeEnum.AD));
    when(vepMapper.getGeneInheritanceMap(vc)).thenReturn(geneInheritanceMap);
    when(genmodInheritanceMapper.mapInheritance(vc)).thenReturn(annotations);

    Map<String, Set<String>> variantGeneList = new HashMap<>();
    variantGeneList.put("1_2_A_T", Set.of("GENE1", "GENE2"));

    Map<String, Annotation> expected = Map.of("FAM",
        Annotation.builder()
            .inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AD).build())).matchingGenes(Collections.singletonList("GENE1"))
            .build());
    assertEquals(expected, inheritanceMatcher.matchInheritance(vc, vepMapper, variantGeneList));
  }

  @Test
  void matchInheritanceArHom() {
    VariantContext vc = mock(VariantContext.class);
    Map<String, Annotation> annotations = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.HOM).build()))
            .build());
    VepMapper vepMapper = mock(VepMapper.class);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = new HashMap<>();
    geneInheritanceMap.put("GENE1", Set.of(InheritanceModeEnum.AR));
    when(vepMapper.getGeneInheritanceMap(vc)).thenReturn(geneInheritanceMap);
    when(genmodInheritanceMapper.mapInheritance(vc)).thenReturn(annotations);

    Map<String, Set<String>> variantGeneList = new HashMap<>();
    variantGeneList.put("1_2_A_T", Set.of("GENE1", "GENE2"));

    Map<String, Annotation> expected = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.HOM).build()))
            .matchingGenes(Collections.singletonList("GENE1")).build());
    assertEquals(expected, inheritanceMatcher.matchInheritance(vc, vepMapper, variantGeneList));
  }

  @Test
  void matchInheritanceArComp() {
    VariantContext vc = mock(VariantContext.class);
    Map<String, Annotation> annotations = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.COMP).build()))
            .build());
    VepMapper vepMapper = mock(VepMapper.class);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = new HashMap<>();
    geneInheritanceMap.put("GENE1", Set.of(InheritanceModeEnum.AR));
    when(vepMapper.getGeneInheritanceMap(vc)).thenReturn(geneInheritanceMap);
    when(genmodInheritanceMapper.mapInheritance(vc)).thenReturn(annotations);
    when(genmodCompoundMapper.mapCompounds(vc))
        .thenReturn(Collections.singletonMap("FAM", new String[]{"1_2_A_T"}));

    Map<String, Set<String>> variantGeneList = new HashMap<>();
    variantGeneList.put("1_2_A_T", Set.of("GENE1", "GENE2"));

    Map<String, Annotation> expected = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.COMP).build()))
            .matchingGenes(Collections.singletonList("GENE1")).build());
    assertEquals(expected, inheritanceMatcher.matchInheritance(vc, vepMapper, variantGeneList));
  }

  @Test
  void matchInheritanceNoMatch() {
    VariantContext vc = mock(VariantContext.class);
    Map<String, Annotation> annotations = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AD).build())).build());
    VepMapper vepMapper = mock(VepMapper.class);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = new HashMap<>();
    geneInheritanceMap.put("GENE1", Set.of(InheritanceModeEnum.AR));
    when(vepMapper.getGeneInheritanceMap(vc)).thenReturn(geneInheritanceMap);
    when(genmodInheritanceMapper.mapInheritance(vc)).thenReturn(annotations);

    Map<String, Set<String>> variantGeneList = new HashMap<>();
    variantGeneList.put("1_2_A_T", Set.of("GENE2", "GENE3"));

    Map<String, Annotation> expected = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AD).build())).matchingGenes(emptyList()).build());
    assertEquals(expected, inheritanceMatcher.matchInheritance(vc, vepMapper, variantGeneList));
  }

  @Test
  void matchInheritanceArCompMismatch() {
    VariantContext vc = mock(VariantContext.class);
    Map<String, Annotation> annotations = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.COMP).build()))
            .build());
    VepMapper vepMapper = mock(VepMapper.class);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = new HashMap<>();
    geneInheritanceMap.put("GENE1", Set.of(InheritanceModeEnum.AR));
    when(vepMapper.getGeneInheritanceMap(vc)).thenReturn(geneInheritanceMap);
    when(genmodInheritanceMapper.mapInheritance(vc)).thenReturn(annotations);
    when(genmodCompoundMapper.mapCompounds(vc))
        .thenReturn(Collections.singletonMap("FAM", new String[]{"1_2_A_T"}));

    Map<String, Set<String>> variantGeneList = new HashMap<>();
    variantGeneList.put("1_2_A_T", Set.of("GENE2", "GENE3"));

    Map<String, Annotation> expected = Map.of("FAM",
        Annotation.builder().inheritanceModes(Set.of(InheritanceMode.builder().inheritanceModeEnum(
                InheritanceModeEnum.AR).subInheritanceMode(SubInheritanceMode.COMP).build()))
            .matchingGenes(emptyList()).build());
    assertEquals(expected, inheritanceMatcher.matchInheritance(vc, vepMapper, variantGeneList));
  }
}