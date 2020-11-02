package org.molgenis.vcf.inheritance.matcher.jannovar;

import de.charite.compbio.jannovar.mendel.bridge.VariantContextMendelianAnnotator;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

public interface JannovarAnnotatorFactory {

  VariantContextMendelianAnnotator create(Trio trio);
}
