package org.molgenis.vcf.inheritance.matcher.checker;

import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.onChromosomeX;

import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Sex;

public class DeNovoChecker {

  private DeNovoChecker() {
  }

  public static boolean checkDeNovo(VariantContext variantContext, Pedigree family,
      Individual individual) {
    if (!hasParents(individual)) {
      return false;
    }
    if (onChromosomeX(variantContext) && individual.getSex() == Sex.MALE) {
      Individual motherIndividual = family.getMembers().get(individual.getMaternalId());
      return motherIndividual == null || isHomRefOrMissingVariant(motherIndividual, variantContext);
    } else {
      return checkRegular(variantContext, family, individual);
    }
  }

  private static boolean hasParents(Individual individual) {
    return !(individual.getMaternalId().isEmpty() || individual.getMaternalId().equals("0")) &&
        !(individual.getPaternalId().isEmpty() || individual.getPaternalId().equals("0"));
  }

  private static boolean checkRegular(VariantContext variantContext, Pedigree family,
      Individual individual) {
    Individual father = family.getMembers().get(individual.getPaternalId());
    Individual mother = family.getMembers().get(individual.getMaternalId());

    Genotype genotype = variantContext.getGenotype(individual.getId());
    boolean sampleHasVariant = !isHomRefOrMissingVariant(individual, variantContext);
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

  public static boolean isHomRefOrMissingVariant(Individual individual, VariantContext variantContext) {
    Genotype genotype = variantContext.getGenotype(individual.getId());
    return genotype == null || !genotype.isCalled() || genotype.getAlleles().stream()
        .noneMatch(allele -> allele.isNonReference() || allele.isNoCall());
  }
}
