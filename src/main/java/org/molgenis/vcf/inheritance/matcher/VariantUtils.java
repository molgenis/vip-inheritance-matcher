package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.model.Sex.MALE;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.model.Sample;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

public class VariantUtils {

  private VariantUtils(){}

  public static String createVariantContextKey(VariantContext vc) {
    return String.format("%s:%s:%s", vc.getContig(), vc.getStart(), vc.getAlleles());
  }

  static Boolean isMendelianViolation(Trio trio, VariantContext vc) {
    Sample proband = trio.getProband();
    Sample mother = trio.getMother();
    Sample father = trio.getFather();
    Boolean isMendelianViolation = null;
    if (vc.getGenotype(proband.getPerson().getIndividualId()).getAlleles().size() > 2) {
      return true;
    }
    if (father.getIndex() != -1 && mother.getIndex() != -1) {
      switch (vc.getContig()) {
        case "X":
          isMendelianViolation = isMendelianViolationX(proband, mother, father, vc);
          break;
        case "Y":
          isMendelianViolation = isMendelianViolationY(proband, father, vc);
          break;
        case "MT":
          isMendelianViolation = isMendelianViolationMT(proband, mother, vc);
          break;
        default://autosomal
          isMendelianViolation = isMendelianViolationAuto(proband, mother, father, vc);
      }
    }
    return isMendelianViolation;
  }

  private static boolean isMendelianViolationAuto(Sample proband, Sample mother,
      Sample father, VariantContext vc) {
    Genotype probandGt = vc.getGenotype(proband.getPerson().getIndividualId());
    Allele allele1 = probandGt.getAllele(0);
    Allele allele2 = probandGt.getAllele(1);
    boolean fatherHasAllele1 = hasVariant(vc, allele1, father);
    boolean fatherHasAllele2 = hasVariant(vc, allele2, father);
    boolean motherHasAllele1 = hasVariant(vc, allele1, mother);
    boolean motherHasAllele2 = hasVariant(vc, allele2, mother);
    return !((fatherHasAllele1 && motherHasAllele2) || (motherHasAllele1 && fatherHasAllele2));
  }

  private static boolean isMendelianViolationMT(Sample proband, Sample mother, VariantContext vc) {
    Genotype probandGt = vc.getGenotype(proband.getPerson().getIndividualId());
    if (probandGt.getAlleles().size() == 1) {
      return !hasVariant(vc, probandGt.getAllele(0), mother);
    } else {
      return true;
    }
  }

  private static boolean isMendelianViolationY(Sample proband, Sample father, VariantContext vc) {
    Genotype probandGt = vc.getGenotype(proband.getPerson().getIndividualId());
    if (proband.getPerson().getSex() == MALE && probandGt.getAlleles().size() == 1) {
     return !hasVariant(vc, probandGt.getAllele(0), father);
    } else {
      //Anything but a single call in a male defeats the rules of inheritance
      return true;
    }
  }

  private static boolean isMendelianViolationX(Sample proband, Sample mother,
      Sample father, VariantContext vc) {
    Genotype probandGt = vc.getGenotype(proband.getPerson().getIndividualId());
    if (proband.getPerson().getSex() == MALE && probandGt.getAlleles().size() == 1) {
      return !hasVariant(vc, probandGt.getAllele(0), mother);
    } else {
      return isMendelianViolationAuto(proband, mother, father, vc);
    }
  }

  private static boolean hasVariant(VariantContext vc, Allele allele, Sample sample) {
    Genotype genotype = vc
        .getGenotype(sample.getPerson().getIndividualId());
    return genotype.getAlleles().contains(allele);
  }
}
