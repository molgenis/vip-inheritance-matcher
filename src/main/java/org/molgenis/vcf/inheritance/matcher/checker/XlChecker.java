package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.*;

import org.molgenis.vcf.inheritance.matcher.VariantGeneRecord;
import org.molgenis.vcf.inheritance.matcher.model.MatchEnum;
import org.molgenis.vcf.utils.sample.model.Pedigree;

public abstract class XlChecker{

  public MatchEnum check(VariantGeneRecord variantGeneRecord, Pedigree family) {
    if (!onChromosomeX(variantGeneRecord)) {
      return FALSE;
    }
    return checkFamily(variantGeneRecord, family);
  }

  protected abstract MatchEnum checkFamily(VariantGeneRecord variantGeneRecord, Pedigree family);
}
