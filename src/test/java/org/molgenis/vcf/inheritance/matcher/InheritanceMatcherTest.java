package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.*;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AD;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.model.*;

class InheritanceMatcherTest {

  @Test
  void matchInheritanceMatch() {
    Inheritance inheritance1 = Inheritance.builder().denovo(true).inheritanceModes(Set.of(InheritanceMode.AD_IP, InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance inheritance2 = Inheritance.builder().denovo(false).inheritanceModes(
        Set.of(AR)).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1, "sample2",
        inheritance2);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", Set.of(AR,AD)), "GENE2", new Gene("GENE2","EntrezGene", Set.of(AD)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
        .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance1 = Inheritance.builder().match(TRUE).denovo(true).inheritanceModes(Set.of(InheritanceMode.AD_IP, InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance expectedInheritance2 = Inheritance.builder().match(TRUE).denovo(false).inheritanceModes(
            Set.of(AR)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation1 = Annotation.builder().inheritance(expectedInheritance1).matchingGenes(Set.of("GENE1","GENE2")).build();
    Annotation annotation2 = Annotation.builder().inheritance(expectedInheritance2).matchingGenes(Set.of("GENE1")).build();
    Map<String, Annotation> expected = Map.of("sample1",annotation1,"sample2",annotation2);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceMismatch() {
    Inheritance inheritance = Inheritance.builder().inheritanceModes(Set.of(InheritanceMode.AD_IP)).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).inheritanceModes(
            Set.of(AD)).inheritanceModes(Set.of(InheritanceMode.AD_IP)).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);

    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceNoSuitableModes() {
    Inheritance inheritance1 = Inheritance.builder().match(FALSE).inheritanceModes(
            emptySet()).inheritanceModes(emptySet()).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", Set.of(AR,AD)),"GENE2",new Gene("GENE2","EntrezGene", Set.of(AD)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).inheritanceModes(
            emptySet()).inheritanceModes(emptySet()).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGene() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(Set.of(InheritanceMode.AD_IP, InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", emptySet()))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(POTENTIAL).inheritanceModes(Set.of(InheritanceMode.AD_IP, InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMatch() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(
            Set.of(AR)).inheritanceModes(Set.of(InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", emptySet()),"GENE2",new Gene("GENE2","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(TRUE).inheritanceModes(Set.of(InheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(Set.of("GENE2")).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMismatch() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(
            Set.of(AD)).compounds(emptySet()).build();

    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VariantContextGenes genes = VariantContextGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", emptySet()),"GENE2",
            new Gene("GENE2","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(POTENTIAL).inheritanceModes(
            Set.of(AD)).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }
}