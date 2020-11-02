package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

public interface InheritanceService {
  Set<InheritanceResults> matchInheritance(
      Map<String, List<VariantContext>> variantsPerGene, Trio trio);
}
