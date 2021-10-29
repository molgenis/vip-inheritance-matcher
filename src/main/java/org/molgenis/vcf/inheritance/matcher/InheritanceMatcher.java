package org.molgenis.vcf.inheritance.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;

public class InheritanceMatcher {

  private InheritanceMatcher() {
  }

  public static Map<String, Annotation> matchInheritance(
      Map<String, Inheritance> inheritanceMap, Collection<Gene> genes) {
    Map<String, Annotation> sampleAnnotationMap = new HashMap<>();
    for (Entry<String, Inheritance> entry : inheritanceMap.entrySet()) {
      Set<String> matchingGenes = new HashSet<>();
      String sample = entry.getKey();
      Inheritance inheritance = entry.getValue();
      for (Gene gene : genes) {
        Set<InheritanceMode> geneInheritanceModes = gene
            .getInheritanceModes();
        if (geneInheritanceModes.stream()
            .anyMatch(mode -> inheritance.getInheritanceModes().contains(mode))) {
          matchingGenes.add(gene.getId());
        }
      }
      Annotation annotation = Annotation.builder().matchingGenes(matchingGenes)
          .inheritance(inheritance).build();
      sampleAnnotationMap.put(sample, annotation);
    }
    return sampleAnnotationMap;
  }
}
