package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.FEMALE;
import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public abstract class XlChecker {

  public boolean check(VariantContext variantContext, Pedigree family) {
    if (!onChromosomeX(variantContext)) {
      return false;
    }
    for (Individual currentIndividual : family.getMembers().values()) {
      Genotype genotype = variantContext.getGenotype(currentIndividual.getId());
      if (!checkSample(variantContext, currentIndividual, genotype)) {
        return false;
      }
    }
    return true;
  }

  protected abstract boolean checkSample(VariantContext variantContext,
      Individual currentIndividual, Genotype genotype);

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
