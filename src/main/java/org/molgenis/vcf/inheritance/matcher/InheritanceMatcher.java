package org.molgenis.vcf.inheritance.matcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.utils.UnexpectedEnumException;

import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;

public class InheritanceMatcher {

  private InheritanceMatcher() {
  }

  public static Map<String, Annotation> matchInheritance(
      Map<String, Inheritance> inheritanceMap, VcfRecordGenes genes) {
    Map<String, Annotation> sampleAnnotationMap = new HashMap<>();
    for (Entry<String, Inheritance> entry : inheritanceMap.entrySet()) {
      Set<String> matchingGenes = new HashSet<>();
      String sample = entry.getKey();
      Inheritance inheritance = entry.getValue();
      //If no inheritance pattern is suitable for the sample, regardless of the gene: inheritance match is false.
      if(inheritance.getPedigreeInheritanceMatches().isEmpty()){
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
    private static void matchGeneInheritance(VcfRecordGenes genes, Set<String> matchingGenes, Inheritance pedigreeInheritance) {
        boolean containsUnknownGene = false;
        for (Gene gene : genes.getGenes().values()) {
          Set<InheritanceMode> geneInheritanceModes = gene
                  .getInheritanceModes();
          if( geneInheritanceModes.isEmpty() ){
              containsUnknownGene = true;
          }
          if (geneInheritanceModes.stream()
                      .anyMatch(geneInheritanceMode -> isMatch(pedigreeInheritance.getPedigreeInheritanceMatches(), geneInheritanceMode))) {
              matchingGenes.add(gene.getId());
              if(pedigreeInheritance.getPedigreeInheritanceMatches().stream().anyMatch(pedigreeInheritanceMatch -> !pedigreeInheritanceMatch.isUncertain())){
                  pedigreeInheritance.setMatch(TRUE);
              }else {
                  pedigreeInheritance.setMatch(POTENTIAL);
              }
          }
        }
        if(matchingGenes.isEmpty()) {
            if (containsUnknownGene || genes.isContainsVcWithoutGene()) {
                pedigreeInheritance.setMatch(POTENTIAL);
            } else {
                pedigreeInheritance.setMatch(FALSE);
            }
        }
    }

    private static Boolean isMatch(Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches, InheritanceMode geneInheritanceMode) {
        for(PedigreeInheritanceMatch pedigreeInheritanceMatch : pedigreeInheritanceMatches) {
            switch (pedigreeInheritanceMatch.getInheritanceMode()) {
                case AD, AD_IP -> {
                    if (geneInheritanceMode == AD) {
                        return true;
                    }
                }
                case AR, AR_C -> {
                    if (geneInheritanceMode == AR) {
                        return true;
                    }
                }
                case XLR, XLD -> {
                    if (geneInheritanceMode == XL) {
                        return true;
                    }
                }
                default -> throw new UnexpectedEnumException(pedigreeInheritanceMatch.getInheritanceMode());
            }
        }
        return false;
    }
}
