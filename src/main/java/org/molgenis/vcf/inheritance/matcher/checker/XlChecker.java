package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;
import static org.molgenis.vcf.utils.sample.model.Sex.FEMALE;
import static org.molgenis.vcf.utils.sample.model.Sex.MALE;
;
import org.molgenis.vcf.inheritance.matcher.EffectiveGenotype;
import org.molgenis.vcf.inheritance.matcher.VcfRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sex;

public abstract class XlChecker extends InheritanceChecker{

  public MatchEnum check(VcfRecord vcfRecord, Pedigree family) {
    if (!onChromosomeX(vcfRecord)) {
      return FALSE;
    }
    return checkFamily(vcfRecord, family);
  }

  protected Sex getSex(Sex sex, EffectiveGenotype genotype) {
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
