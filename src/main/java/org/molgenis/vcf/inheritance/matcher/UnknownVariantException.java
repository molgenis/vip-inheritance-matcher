package org.molgenis.vcf.inheritance.matcher;

public class UnknownVariantException extends
    RuntimeException {

  public UnknownVariantException(String variantKey) {
    super(String.format("Cannot find a variant for compound key '%s'.", variantKey));
  }
}
