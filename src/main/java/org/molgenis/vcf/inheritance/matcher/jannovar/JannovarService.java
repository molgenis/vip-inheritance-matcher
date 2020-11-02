package org.molgenis.vcf.inheritance.matcher.jannovar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import de.charite.compbio.jannovar.mendel.bridge.CannotAnnotateMendelianInheritance;
import de.charite.compbio.jannovar.mendel.bridge.VariantContextMendelianAnnotator;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.InheritanceService;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.springframework.stereotype.Component;

@Component
public class JannovarService implements InheritanceService {

  private final JannovarAnnotatorFactory jannovarAnnotatorFactory;

  public JannovarService(JannovarAnnotatorFactory jannovarAnnotatorFactory) {
    this.jannovarAnnotatorFactory = jannovarAnnotatorFactory;
  }

  @Override
  public Set<InheritanceResults> matchInheritance(Map<String, List<VariantContext>> variantsPerGene,
      Trio trio) {
    Set<InheritanceResults> results = new HashSet<>();
    VariantContextMendelianAnnotator jannovarMendelianAnnotator = jannovarAnnotatorFactory
        .create(trio);
    for (Entry<String, List<VariantContext>> variantsPerGeneEntry : variantsPerGene.entrySet()) {
      Map<String, Set<InheritanceMode>> inheritanceModesForGene = matchInheritanceForGene(
          variantsPerGeneEntry.getValue(), jannovarMendelianAnnotator);
      results.add(InheritanceResults.builder().gene(
          variantsPerGeneEntry.getKey()).variantInheritanceResults(inheritanceModesForGene)
          .trio(trio)
          .build());
    }
    return results;
  }

  private Map<String, Set<InheritanceMode>> matchInheritanceForGene(List<VariantContext> variants,
      VariantContextMendelianAnnotator jannovarMendelianAnnotator) {
    try {
      ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> compatibleInheritanceModes = jannovarMendelianAnnotator
          .computeCompatibleInheritanceSubModes(variants);
      return JannovarResultMapper.map(compatibleInheritanceModes);
    } catch (CannotAnnotateMendelianInheritance cannotAnnotateMendelianInheritance) {
      throw new JannovarAnnotatorException(cannotAnnotateMendelianInheritance);
    }
  }
}
