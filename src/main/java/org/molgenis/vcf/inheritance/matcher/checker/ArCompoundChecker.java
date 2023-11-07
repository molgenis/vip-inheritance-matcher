package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onAutosome;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;

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
      Map<String, Gene> genes = vepMapper.getGenes(variantContext).getGenes();
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
    for (Sample sample : family.getMembers().values()) {
      if (!checkIndividual(variantContext, otherVariantContext, sample)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkIndividual(VariantContext variantContext, VariantContext otherVariantContext,
      Sample sample) {
    //Affected individuals have to be het. for both variants
    //Healthy individuals can be het. for one of the variants but cannot have both variants
    Genotype sampleGt = variantContext.getGenotype(sample.getPerson().getIndividualId());
    Genotype sampleOtherGt = otherVariantContext.getGenotype(sample.getPerson().getIndividualId());

    switch (sample.getPerson().getAffectedStatus()) {
      case AFFECTED:
        return checkAffectedSample(sampleGt, sampleOtherGt);
      case UNAFFECTED:
        return checkUnaffectedSample(sampleGt, sampleOtherGt);
      case MISSING:
        return true;
      default:
        throw new IllegalArgumentException();
    }
  }

  private boolean checkUnaffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
    if(sampleGt == null || sampleOtherGt == null){
      return true;
    }
    boolean sampleContainsAlt = !sampleGt.getAlleles().stream()
        .allMatch(allele -> allele.isReference() || allele
            .isNoCall());
    boolean sampleOtherGtContainsAlt = !sampleOtherGt.getAlleles().stream()
        .allMatch(allele -> allele.isReference() || allele
            .isNoCall());
    //Check if one or both of the variants might not be present (REF or missing) in a unaffected individual.
    //Only if both are present the check fails.
    if (sampleContainsAlt && sampleOtherGtContainsAlt) {
      if (sampleGt.isPhased() && sampleOtherGt.isPhased()) {
        return checkPhasedUnaffected(sampleGt, sampleOtherGt);
      }
      return false;
    }
    return true;
  }

  private boolean checkPhasedUnaffected(Genotype sampleGt, Genotype sampleOtherGt) {
    Allele allele1 = sampleGt.getAllele(0);
    Allele allele2 = sampleGt.getAllele(1);
    Allele otherAllele1 = sampleOtherGt.getAllele(0);
    Allele otherAllele2 = sampleOtherGt.getAllele(1);
    //For phased data both variants can be present in a unaffected individual if both are on the same allele
    return !(bothAlt(allele1, otherAllele2) || bothAlt(allele2, otherAllele1));
  }

  private boolean bothAlt(Allele allele1, Allele allele2) {
    return allele1.isNonReference() && allele2.isNonReference();
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
