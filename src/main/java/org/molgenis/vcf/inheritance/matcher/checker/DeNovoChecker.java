package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;

public class DeNovoChecker {

  public boolean checkDeNovo(VariantContext variantContext, Pedigree family,
      Sample sample) {
    if (!hasParents(sample)) {
      return false;
    }
    if (onChromosomeX(variantContext) && sample.getPerson().getSex() == Sex.MALE) {
      Sample motherIndividual = family.getMembers().get(sample.getPerson().getMaternalId());
      return motherIndividual == null || isHomRefOrMissingVariant(motherIndividual, variantContext);
    } else {
      return checkRegular(variantContext, family, sample);
    }
  }

  private boolean hasParents(Sample sample) {
    return !(sample.getPerson().getMaternalId().isEmpty() || sample.getPerson().getMaternalId().equals("0")) &&
        !(sample.getPerson().getPaternalId().isEmpty() || sample.getPerson().getPaternalId().equals("0"));
  }

  private boolean checkRegular(VariantContext variantContext, Pedigree family,
      Sample sample) {
    Sample father = family.getMembers().get(sample.getPerson().getPaternalId());
    Sample mother = family.getMembers().get(sample.getPerson().getMaternalId());

    Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
    boolean sampleHasVariant = !isHomRefOrMissingVariant(sample, variantContext);
    boolean fatherHasVariant =
        father == null || !isHomRefOrMissingVariant(father, variantContext);
    boolean motherHasVariant =
        mother == null || !isHomRefOrMissingVariant(mother, variantContext);

    boolean result = false;
    if (genotype != null) {
      if (genotype.isHom()) {
        result = sampleHasVariant && !(fatherHasVariant && motherHasVariant);
      } else {
        result = sampleHasVariant && !(fatherHasVariant || motherHasVariant);
      }
    }
    return result;
  }

  public boolean isHomRefOrMissingVariant(Sample sample, VariantContext variantContext) {
    Genotype genotype = variantContext.getGenotype(sample.getPerson().getIndividualId());
    return genotype == null || !genotype.isCalled() || genotype.getAlleles().stream()
        .noneMatch(allele -> allele.isNonReference() || allele.isNoCall());
  }
}
