package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.*;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AD;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

class InheritanceMatcherTest {

  @Test
  void matchInheritanceMatch() {
    Inheritance inheritance1 = Inheritance.builder().denovo(true).inheritanceModes(
        Set.of(AD,AR)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP, SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance inheritance2 = Inheritance.builder().denovo(false).inheritanceModes(
        Set.of(AR)).subInheritanceModes(emptySet()).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1, "sample2",
        inheritance2);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", true, Set.of(AR,AD)),new Gene("GENE2","EntrezGene", true, Set.of(AD)));

    Map<String, Annotation> actual = InheritanceMatcher
        .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance1 = Inheritance.builder().match(TRUE).denovo(true).inheritanceModes(
            Set.of(AD,AR)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP, SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance expectedInheritance2 = Inheritance.builder().match(TRUE).denovo(false).inheritanceModes(
            Set.of(AR)).subInheritanceModes(emptySet()).compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation1 = Annotation.builder().inheritance(expectedInheritance1).matchingGenes(Set.of("GENE1","GENE2")).build();
    Annotation annotation2 = Annotation.builder().inheritance(expectedInheritance2).matchingGenes(Set.of("GENE1")).build();
    Map<String, Annotation> expected = Map.of("sample1",annotation1,"sample2",annotation2);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceMismatch() {
    Inheritance inheritance = Inheritance.builder().inheritanceModes(
            Set.of(AD)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP)).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", true, Set.of(AR)));

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).inheritanceModes(
            Set.of(AD)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP)).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);

    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceNoSuitableModes() {
    Inheritance inheritance1 = Inheritance.builder().match(FALSE).inheritanceModes(
            emptySet()).subInheritanceModes(emptySet()).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", false, Set.of(AR,AD)),new Gene("GENE2","EntrezGene", false, Set.of(AD)));

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).inheritanceModes(
            emptySet()).subInheritanceModes(emptySet()).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGene() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(
            Set.of(AD,AR)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP, SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", true, emptySet()));

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(UNKNOWN).inheritanceModes(
            Set.of(AR, AD)).subInheritanceModes(Set.of(SubInheritanceMode.AD_IP, SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMatch() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(
            Set.of(AR)).subInheritanceModes(Set.of(SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", false, emptySet()),new Gene("GENE2","EntrezGene", false, Set.of(AR)));

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(TRUE).inheritanceModes(
            Set.of(AR)).subInheritanceModes(Set.of(SubInheritanceMode.AR_C)).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(Set.of("GENE2")).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMismatch() {
    Inheritance inheritance1 = Inheritance.builder().inheritanceModes(
            Set.of(AD)).subInheritanceModes(emptySet()).compounds(emptySet()).build();

    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    Collection<Gene> genes = Set.of(new Gene("GENE1","EntrezGene", false, emptySet()),new Gene("GENE2","EntrezGene", false, Set.of(AR)));

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(UNKNOWN).inheritanceModes(
            Set.of(AD)).subInheritanceModes(emptySet()).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }
}