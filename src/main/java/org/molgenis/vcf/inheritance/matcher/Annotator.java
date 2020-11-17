package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.springframework.stereotype.Component;

@Component
public class Annotator {

  VCFHeader annotateHeader(VCFHeader vcfHeader) {
    vcfHeader.addMetaDataLine(new VCFInfoHeaderLine("VIP_INH", VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "VIP Inheritance information in the format: FamilyID|PredictedInheritanceModels|Denove|MatchingGenes"));
    return vcfHeader;
  }

  VariantContext annotate(VariantContext vc, List<Annotation> annotations) {
    VariantContextBuilder variantContextBuilder = new VariantContextBuilder(vc);
    List<String> annotationValues = annotations.stream()
        .map(this::toAnnotationString).collect(
            Collectors.toList());
    variantContextBuilder.attribute("VIP_INH", annotationValues);
    return variantContextBuilder.make();
  }

  private String toAnnotationString(Annotation annotation) {
    return String.format("%s|%s|%s|%s", annotation.getFamilyID(),
        mapInheritanceModesToString(annotation.getInheritanceMode()), annotation.isDenovo(),
        String.join("&", annotation.getMatchingGenes()));
  }

  private String mapInheritanceModesToString(Set<InheritanceMode> inheritanceModes) {
    String result = "";
    if (inheritanceModes != null) {
      result = String.join("&", inheritanceModes.stream()
          .map(this::mapInheritanceModeToString)
          .collect(Collectors.toSet()));
    }
    return result;
  }

  private String mapInheritanceModeToString(InheritanceMode inheritanceMode) {
    StringBuilder result = new StringBuilder();
    result.append(inheritanceMode.getInheritanceModeEnum());
    if (inheritanceMode.getSubInheritanceMode() != null) {
      result.append(":");
      result.append(inheritanceMode.getSubInheritanceMode());
    }
    return result.toString();
  }
}
