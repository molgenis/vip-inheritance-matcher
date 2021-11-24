package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.ChromosomeUtils;
import org.molgenis.vcf.inheritance.matcher.model.Chromosome;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public abstract class XlChecker {
  public boolean check(VariantContext variantContext, Map<String, Sample> family) {
    if (ChromosomeUtils.mapChromosomeId(variantContext.getContig()) != Chromosome.X) {
      return false;
    }
    for (Sample currentSample : family.values()) {
      Genotype genotype = variantContext.getGenotype(currentSample.getIndividualId());
      if (!checkSample(variantContext, currentSample, genotype)) {
        return false;
      }
    }
    return true;
  }

  protected abstract boolean checkSample(VariantContext variantContext,
      Sample currentSample, Genotype genotype);

  protected Sex getSex(Sex sex, Genotype genotype) {
    if (sex == Sex.UNKNOWN) {
      //UNKNOWN? use best guess based on number of alleles
      if (genotype.getAlleles().size() == 1) {
        return MALE;
      } else {
        return FEMALE;
      }
    }
    return sex;
  }
}
