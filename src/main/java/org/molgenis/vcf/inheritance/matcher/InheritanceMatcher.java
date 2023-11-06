package org.molgenis.vcf.inheritance.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.vcf.inheritance.matcher.model.*;

import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMatch.*;

public class InheritanceMatcher {

  private InheritanceMatcher() {
  }

  public static Map<String, Annotation> matchInheritance(
      Map<String, Inheritance> inheritanceMap, VariantContextGenes genes) {
    Map<String, Annotation> sampleAnnotationMap = new HashMap<>();
    for (Entry<String, Inheritance> entry : inheritanceMap.entrySet()) {
      Set<String> matchingGenes = new HashSet<>();
      String sample = entry.getKey();
      Inheritance inheritance = entry.getValue();
      //If no inheritance pattern is suitable for the sample, regardless of the gene: inheritance match is false.
      if(inheritance.getInheritanceModes().isEmpty()){
        inheritance.setMatch(FALSE);
      }
      else {
          matchGeneInheritance(genes, matchingGenes, inheritance);
      }
      Annotation annotation = Annotation.builder().matchingGenes(matchingGenes)
          .inheritance(inheritance).build();
      sampleAnnotationMap.put(sample, annotation);
    }
    return sampleAnnotationMap;
  }

    /**
     * If there are one or more matches between sample inheritance modes and gene inheritance modes:
     * - inheritance match is true
     * If there are no matches between sample inheritance modes and gene inheritance modes:
     *  - inheritance match is unknown if any genes for the variant have unknown inheritance pattern.
     *  - inheritance match is false if all genes for the variant have known (but mismatching) inheritance pattern.
     */
    private static void matchGeneInheritance(VariantContextGenes genes, Set<String> matchingGenes, Inheritance inheritance) {
        boolean containsUnknownGene = false;
        for (Gene gene : genes.getGenes().values()) {
          Set<InheritanceMode> geneInheritanceModes = gene
                  .getInheritanceModes();
          if( geneInheritanceModes.isEmpty() ){
              containsUnknownGene = true;
          }
          if (geneInheritanceModes.stream()
                      .anyMatch(mode -> inheritance.getInheritanceModes().contains(mode))) {
              matchingGenes.add(gene.getId());
              inheritance.setMatch(TRUE);
          }
        }
        if(matchingGenes.isEmpty()) {
            if (containsUnknownGene || genes.isContainsVcWithoutGene()) {
                inheritance.setMatch(UNKNOWN);
            } else {
                inheritance.setMatch(FALSE);
            }
        }
    }
}
