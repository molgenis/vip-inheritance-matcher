package org.molgenis.vcf.inheritance.matcher.checker;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.ChromsomeUtils;
import org.molgenis.vcf.inheritance.matcher.model.Chromosome;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class DeNovoChecker {

  private DeNovoChecker() {
  }

  public static boolean checkDeNovo(VariantContext variantContext, Map<String, Sample> family,
      Sample sample) {
    if(!hasParents(sample)){
      return false;
    }
    if ((ChromsomeUtils.mapChromosomeID(variantContext.getContig()) == Chromosome.X) && sample.getSex() == Sex.MALE) {
      Sample motherSample = family.get(sample.getMaternalId());
      return motherSample == null || isHomRefOrMissingVariant(motherSample, variantContext);
    } else {
      return checkRegular(variantContext, family, sample);
    }
  }

  private static boolean hasParents(Sample sample) {
    return !(sample.getMaternalId().isEmpty() || sample.getMaternalId().equals("0")) &&
        !(sample.getPaternalId().isEmpty() || sample.getPaternalId().equals("0"));
  }

  private static boolean checkRegular(VariantContext variantContext, Map<String, Sample> family,
      Sample sample) {
    Sample father = family.get(sample.getPaternalId());
    Sample mother = family.get(sample.getMaternalId());

    Genotype genotype = variantContext.getGenotype(sample.getIndividualId());
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

  public static boolean isHomRefOrMissingVariant(Sample sample, VariantContext variantContext) {
    Genotype genotype = variantContext.getGenotype(sample.getIndividualId());
    return genotype == null || !genotype.isCalled() || genotype.getAlleles().stream()
        .noneMatch(allele -> variantContext.getAlternateAlleles().contains(allele));
  }
}
