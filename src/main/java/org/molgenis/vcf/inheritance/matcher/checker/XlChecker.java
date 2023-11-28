package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.utils.sample.model.Sex.FEMALE;
import static org.molgenis.vcf.utils.sample.model.Sex.MALE;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sex;

public abstract class XlChecker extends InheritanceChecker{

  public MatchEnum check(VariantContext variantContext, Pedigree family) {
    if (!onChromosomeX(variantContext)) {
      return FALSE;
    }
    return checkFamily(variantContext, family);
  }

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
