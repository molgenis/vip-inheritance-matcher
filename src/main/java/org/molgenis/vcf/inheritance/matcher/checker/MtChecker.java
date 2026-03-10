package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.checker.CheckerUtils.merge;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import htsjdk.variant.variantcontext.Allele;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class MtChecker extends InheritanceChecker {
  public MatchEnum check(VcfRecord vcfRecord, Pedigree family) {
    if (!VariantContextUtils.onChromosomeMt(vcfRecord)) {
      return FALSE;
    }

    return checkFamily(vcfRecord, family);
  }

  @Override
  protected MatchEnum checkUnaffected(
      VcfRecord vcfRecord,
      Map<AffectedStatus, Set<Sample>> membersByStatus,
      Set<Allele> affectedAlleles) {
    Set<MatchEnum> matches = new HashSet<>();
    Set<Sample> samples = membersByStatus.get(AffectedStatus.UNAFFECTED);
    if (samples != null) {
      for (Sample unAffectedSample : samples) {
        Genotype genotype = vcfRecord.getGenotype(unAffectedSample.getPerson().getIndividualId());
        if (genotype != null
            && (genotype.isHomRef() || (genotype.getPloidy() == 1 && genotype.hasReference()))) {
          matches.add(TRUE);
        } else if (genotype != null
            && genotype.getAlleles().stream()
                .allMatch(allele -> allele.isCalled() && affectedAlleles.contains(allele))) {
          matches.add(FALSE);
        } else {
          matches.add(POTENTIAL);
        }
      }
    }
    return merge(matches);
  }

  @Override
  protected MatchEnum checkAffected(
      VcfRecord vcfRecord,
      Map<AffectedStatus, Set<Sample>> membersByStatus,
      Set<Genotype> affectedGenotypes) {
    Set<MatchEnum> matches = new HashSet<>();
    Set<Sample> samples = membersByStatus.get(AffectedStatus.AFFECTED);
    if (samples != null) {
      for (Sample affectedSample : samples) {
        Genotype genotype = vcfRecord.getGenotype(affectedSample.getPerson().getIndividualId());
        affectedGenotypes.add(genotype);
        if (genotype != null && !genotype.hasAltAllele() && genotype.isCalled()) {
          return FALSE;
        } else if (genotype == null || genotype.isNoCall() || genotype.isMixedOrHet()) {
          matches.add(POTENTIAL);
        } else {
          matches.add(TRUE);
        }
      }
    }
    return merge(matches);
  }
}
