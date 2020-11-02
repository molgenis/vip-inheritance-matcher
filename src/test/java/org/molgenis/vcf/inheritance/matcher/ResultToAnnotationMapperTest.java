package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.*;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

@ExtendWith(MockitoExtension.class)
class ResultToAnnotationMapperTest {

  @Test
  void map() {
    Trio trio = null;
    Set<InheritanceResults> trioInheritanceResults = new HashSet<>();
    InheritanceMode inheritanceMode1 = InheritanceMode.builder().mode(AR).build();
    Map<String, Set<InheritanceMode>> variantInheritanceResults = Map.of("1:1:[A*,T]", Set.of(inheritanceMode1));
    trioInheritanceResults.add(InheritanceResults.builder().trio(trio).gene("GENE").variantInheritanceResults(variantInheritanceResults).build());
    VariantContext vc = mock(VariantContext.class);
    doReturn("1").when(vc).getContig();
    doReturn(Arrays.asList("A*,T")).when(vc).getAlleles();
    doReturn(1).when(vc).getStart();

    Annotation expected = new Annotation(Arrays.asList("GENE|AR|"));
    expected.setMendelianViolation("1");

    Annotation actual = ResultToAnnotationMapper
        .map(trioInheritanceResults, vc, true);

    assertEquals(expected, actual);
  }

  @Test
  void mapCompound() {
    Trio trio = null;
    Set<InheritanceResults> trioInheritanceResults = new HashSet<>();
    InheritanceMode inheritanceMode1 = InheritanceMode.builder().mode(AR).isCompound(false).build();
    Map<String, Set<InheritanceMode>> variantInheritanceResults = Map.of("1:1:[A*,T]", Set.of(inheritanceMode1));
    trioInheritanceResults.add(InheritanceResults.builder().trio(trio).gene("GENE").variantInheritanceResults(variantInheritanceResults).build());
    VariantContext vc = mock(VariantContext.class);
    doReturn("1").when(vc).getContig();
    doReturn(Arrays.asList("A*,T")).when(vc).getAlleles();
    doReturn(1).when(vc).getStart();

    Annotation expected = new Annotation(Arrays.asList("GENE|AR|0"));
    expected.setMendelianViolation("1");

    Annotation actual = ResultToAnnotationMapper
        .map(trioInheritanceResults, vc, true);

    assertEquals(expected, actual);
  }
}