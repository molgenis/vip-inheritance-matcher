package org.molgenis.vcf.inheritance.matcher.jannovar;

import de.charite.compbio.jannovar.mendel.bridge.VariantContextMendelianAnnotator;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.springframework.stereotype.Component;

@Component
public class JannovarAnnotatorFactoryImpl  implements JannovarAnnotatorFactory{
  @Override
  public VariantContextMendelianAnnotator create(Trio trio) {
    Pedigree jannovarPed = JannovarPedigreeMapper.map(trio);
    return new VariantContextMendelianAnnotator(
        jannovarPed, false, false);
  }
}
