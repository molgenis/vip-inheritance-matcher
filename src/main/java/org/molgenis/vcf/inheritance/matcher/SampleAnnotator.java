package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Trio;

public interface SampleAnnotator {

  VariantContext annotate(VariantContext variantContext,
      Map<Trio, Annotation> annotations);

  VCFHeader addMetadata(VCFHeader fileHeader, List<Path> pedfiles);
}
