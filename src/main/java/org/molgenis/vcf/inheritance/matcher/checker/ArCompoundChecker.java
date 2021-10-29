package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.Sample;

public class ArCompoundChecker {

  private final VepMapper vepMapper;

  public ArCompoundChecker(VepMapper vepMapper) {
    this.vepMapper = vepMapper;
  }

  public List<VariantContext> check(
      Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Map<String, Sample> family) {
    if (!(variantContext.getContig().equals("X") || variantContext.getContig().equals("chrX"))) {
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
      VariantContext variantContext, Map<String, Sample> family, List<VariantContext> compounds,
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

  private boolean checkFamily(Map<String, Sample> family, VariantContext variantContext,
      VariantContext otherVariantContext) {
    for (Sample sample : family.values()) {
      //Affected individuals have to be het. for both variants
      //Healthy individuals can be het. for one of the variants but cannot have both variants
      Genotype sampleGt = variantContext.getGenotype(sample.getIndividualId());
      Genotype sampleOtherGt = otherVariantContext.getGenotype(sample.getIndividualId());

      if (sample.getAffectedStatus() == AffectedStatus.AFFECTED) {
        if (!checkAffectedSample(sampleGt, sampleOtherGt)) {
          return false;
        }
      } else {
        if (!checkUnaffectedSample(sampleGt, sampleOtherGt)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkUnaffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
    if (sampleGt.isHet() && sampleGt.isCalled() && sampleOtherGt.isHet() && sampleOtherGt
        .isCalled()) {
      //if data is phased, the variants can occur in unaffected individuals as long as they are on the same allele
      return sampleGt.isPhased() && sampleOtherGt.isPhased() && (sampleGt.getAllele(0)
          .equals(sampleOtherGt.getAllele(0)));
    }
    return true;
  }

  private boolean checkAffectedSample(Genotype sampleGt, Genotype sampleOtherGt) {
    if (!(sampleGt.isHet() && sampleOtherGt.isHet())) {
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
