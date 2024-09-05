package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.model.*;

class InheritanceMatcherTest {

  @Test
  void matchInheritanceMatch() {
    Inheritance inheritance1 = Inheritance.builder().denovo(TRUE).pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP,false), new PedigreeInheritanceMatch(InheritanceMode.AR_C,false))).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance inheritance2 = Inheritance.builder().denovo(FALSE).pedigreeInheritanceMatches(
        Set.of(new PedigreeInheritanceMatch(AR,false))).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1, "sample2",
        inheritance2);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", Set.of(AR,AD)), "GENE2", new Gene("GENE2","EntrezGene", Set.of(AD)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
        .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance1 = Inheritance.builder().match(TRUE).denovo(TRUE).pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP,false), new PedigreeInheritanceMatch(InheritanceMode.AR_C,false))).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance expectedInheritance2 = Inheritance.builder().match(TRUE).denovo(FALSE).pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AR,false))).compounds(singleton("OTHER_VARIANT")).build();
    Annotation annotation1 = Annotation.builder().inheritance(expectedInheritance1).matchingGenes(Set.of("GENE1","GENE2")).build();
    Annotation annotation2 = Annotation.builder().inheritance(expectedInheritance2).matchingGenes(Set.of("GENE1")).build();
    Map<String, Annotation> expected = Map.of("sample1",annotation1,"sample2",annotation2);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceMismatch() {
    Inheritance inheritance = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP,false))).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AD_IP, false))).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);

    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceNoSuitableModes() {
    Inheritance inheritance1 = Inheritance.builder().match(FALSE).pedigreeInheritanceMatches(
            emptySet()).pedigreeInheritanceMatches(emptySet()).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", Set.of(AR,AD)),"GENE2",new Gene("GENE2","EntrezGene", Set.of(AD)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(FALSE).pedigreeInheritanceMatches(
            emptySet()).pedigreeInheritanceMatches(emptySet()).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGene() {
    Inheritance inheritance1 = Inheritance.builder().pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP,false), new PedigreeInheritanceMatch(InheritanceMode.AR_C,false))).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", emptySet()))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(POTENTIAL).pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(AD_IP,false), new PedigreeInheritanceMatch(InheritanceMode.AR_C,false))).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMatch() {
    Inheritance inheritance1 = Inheritance.builder().pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AR_C, false))).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1",new Gene("GENE1","EntrezGene", emptySet()),"GENE2",new Gene("GENE2","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(TRUE).pedigreeInheritanceMatches(Set.of(new PedigreeInheritanceMatch(InheritanceMode.AR_C,false))).compounds(singleton("OTHER_VARIANT")).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(Set.of("GENE2")).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }

  @Test
  void matchInheritanceUnknownGeneAndKnownGeneMismatch() {
    Inheritance inheritance1 = Inheritance.builder().pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AD,false))).compounds(emptySet()).build();

    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1);
    VcfRecordGenes genes = VcfRecordGenes.builder().genes(Map.of("GENE1", new Gene("GENE1","EntrezGene", emptySet()),"GENE2",
            new Gene("GENE2","EntrezGene", Set.of(AR)))).build();

    Map<String, Annotation> actual = InheritanceMatcher
            .matchInheritance(inheritanceMap, genes);

    Inheritance expectedInheritance = Inheritance.builder().match(POTENTIAL).pedigreeInheritanceMatches(
            Set.of(new PedigreeInheritanceMatch(AD,false))).compounds(emptySet()).build();
    Annotation expectedAnnotation = Annotation.builder().inheritance(expectedInheritance).matchingGenes(emptySet()).build();
    Map<String, Annotation> expected = Map.of("sample1",expectedAnnotation);
    assertEquals(expected, actual);
  }
}