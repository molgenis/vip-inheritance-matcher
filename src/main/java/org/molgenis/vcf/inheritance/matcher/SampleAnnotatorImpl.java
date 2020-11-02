package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.springframework.stereotype.Component;

@Component
public class SampleAnnotatorImpl implements SampleAnnotator {

  static final String MENDELIAN_VIOLATION_KEY = "MV";
  static final String INHERITANCE_MODE_KEY = "IHM";
  static final String HEADER_VIP_INHERITANCE = "VIP_inheritance";

  @Override
  public VariantContext annotate(VariantContext variantContext,
      Map<Trio, Annotation> annotations) {
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(variantContext);
    GenotypesContext genotypesContext = GenotypesContext.copy(variantContext.getGenotypes());
    for (Entry<Trio, Annotation> annotationEntry : annotations.entrySet()) {
      String sampleId = annotationEntry.getKey().getProband().getPerson().getIndividualId();
      GenotypeBuilder genotypeBuilder = new GenotypeBuilder(
          variantContext.getGenotype(sampleId));
      Annotation annotation = annotationEntry.getValue();
      if (!annotation.getInheritanceMode().isEmpty()) {
        genotypeBuilder.attribute(INHERITANCE_MODE_KEY, annotation.getInheritanceMode());
      }
      if (annotation.getMendelianViolation() != null) {
        genotypeBuilder.attribute(MENDELIAN_VIOLATION_KEY, annotation.getMendelianViolation());
      }
      genotypesContext.replace(genotypeBuilder.make());
    }
    return variantContextBuilder.genotypes(genotypesContext).make();
  }

  @Override
  public VCFHeader addMetadata(VCFHeader fileHeader, List<Path> pedFiles) {
    String pedFileNames = pedFiles.stream().map(pedfile -> pedfile.getFileName().toString()).collect(Collectors.joining(","));
    fileHeader.addMetaDataLine(new VCFHeaderLine(HEADER_VIP_INHERITANCE, String.format("Inheritance annotations based on ped files: %s",pedFileNames)));
    fileHeader
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODE_KEY, 1, VCFHeaderLineType.String,
            "Inheritance mode and compound information for this sample in the format: GENE1|MODE|COMPOUND,GENE2|MODE|COMPOUND"));
    fileHeader
        .addMetaDataLine(
            new VCFFormatHeaderLine(MENDELIAN_VIOLATION_KEY, 1, VCFHeaderLineType.Integer,
                "Indication if the variant is a mendelian violation for this sample."));
    return fileHeader;
  }
}
