package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;

public class ArCompoundChecker {

  private final VepMapper vepMapper;

  public ArCompoundChecker(VepMapper vepMapper) {
    this.vepMapper = vepMapper;
  }

  public List<VariantContext> check(
      Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Pedigree family) {
    if (onAutosome(variantContext)) {
      List<VariantContext> compounds = new ArrayList<>();
      Map<String, Gene> genes = vepMapper.getGenes(variantContext);
      for (Gene gene : genes.values()) {
        checkForGene(geneVariantMap, variantContext, family, compounds, gene);
      }
      return compounds;
    }
    return Collections.emptyList();
  }

  private void checkForGene(Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Pedigree family, List<VariantContext> compounds,
      Gene gene) {
    List<VariantContext> variantContexts = geneVariantMap.get(gene.getId());
    if (variantContexts != null) {
      for (VariantContext otherVariantContext : variantContexts) {
        if (!otherVariantContext.equals(variantContext) && checkFamily(family, variantContext,
            otherVariantContext)) {
          compounds.add(otherVariantContext);
        }
      }
    }
  }

  private boolean checkFamily(Pedigree family, VariantContext variantContext,
      VariantContext otherVariantContext) {
    for (Individual individual : family.getMembers().values()) {
      if (!checkIndividual(variantContext, otherVariantContext, individual)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkIndividual(VariantContext variantContext, VariantContext otherVariantContext,
      Individual individual) {
    //Affected individuals have to be het. for both variants
    //Healthy individuals can be het. for one of the variants but cannot have both variants
    Genotype sampleGt = variantContext.getGenotype(individual.getId());
    Genotype sampleOtherGt = otherVariantContext.getGenotype(individual.getId());

    switch (individual.getAffectedStatus()) {
      case AFFECTED:
        return checkAffectedSample(sampleGt, sampleOtherGt);
      case UNAFFECTED:
        return checkUnaffectedSample(sampleGt, sampleOtherGt, variantContext);
      case UNKNOWN:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }

  private boolean checkUnaffectedSample(Genotype sampleGt, Genotype sampleOtherGt,
      VariantContext variantContext) {
    boolean sampleContainsAlt = sampleGt.getAlleles().stream()
        .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
    boolean otherSampleContainsAlt = sampleOtherGt.getAlleles().stream()
        .anyMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
    if (sampleContainsAlt && otherSampleContainsAlt) {
      //if data is phased, the variants can occur in unaffected individuals as long as they are on the same allele
      if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
        return sampleGt.getAllele(0).equals(sampleOtherGt.getAllele(0));
      } else {
        return false;
      }
    }
    return true;
  }

  private boolean checkAffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
    if (!((sampleGt.isHet() || sampleGt.isMixed()) && (sampleOtherGt.isHet() || sampleOtherGt
        .isMixed()))) {
      return false;
    } else {
      if (sampleGt.isPhased() && sampleOtherGt.isPhased() && sampleGt.getAllele(0)
          .equals(sampleOtherGt.getAllele(0))) {
        return false;
      }
    }
    return true;
  }
}
