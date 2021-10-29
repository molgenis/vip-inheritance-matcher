package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
  void matchInheritance() {
    Inheritance inheritance1 = Inheritance.builder().denovo(true).inheritanceModes(
        Set.of(AD,AR)).subInheritanceModes(Set.of(SubInheritanceMode.AD_NON_PENETRANCE, SubInheritanceMode.AR_COMPOUND)).compounds(singleton("OTHER_VARIANT")).build();
    Inheritance inheritance2 = Inheritance.builder().denovo(false).inheritanceModes(
        Set.of(AR)).subInheritanceModes(emptySet()).compounds(singleton("OTHER_VARIANT")).build();
    Map<String, Inheritance> inheritanceMap = Map.of("sample1", inheritance1, "sample2",
        inheritance2);
    Collection<Gene> genes = Set.of(new Gene("GENE1",Set.of(AR,AD)),new Gene("GENE2",Set.of(AD)));

    Map<String, Annotation> actual = InheritanceMatcher
        .matchInheritance(inheritanceMap, genes);

    Annotation annotation1 = Annotation.builder().inheritance(inheritance1).matchingGenes(Set.of("GENE1","GENE2")).build();
    Annotation annotation2 = Annotation.builder().inheritance(inheritance2).matchingGenes(Set.of("GENE1")).build();
    Map<String, Annotation> expected = Map.of("sample1",annotation1,"sample2",annotation2);
    assertEquals(expected, actual);
  }
}