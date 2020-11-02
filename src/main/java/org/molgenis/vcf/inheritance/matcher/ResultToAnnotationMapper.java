package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.VariantUtils.createVariantContextKey;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;

public class ResultToAnnotationMapper {

  private ResultToAnnotationMapper() {
  }

  public static Annotation map(
      Set<InheritanceResults> trioInheritanceResults,
      VariantContext variantContext, Boolean isMendelianViolation) {
    Annotation annotation = createInheritanceModeAnnotation(trioInheritanceResults, variantContext);
    String mendelianViolationValue = null;
    if (Boolean.TRUE.equals(isMendelianViolation)) {
      mendelianViolationValue = "1";
    } else if (isMendelianViolation != null) {
      mendelianViolationValue = "0";
    }
    annotation.setMendelianViolation(mendelianViolationValue);
    return annotation;
  }

  private static Annotation createInheritanceModeAnnotation(
      Set<InheritanceResults> trioInheritanceResults, VariantContext variantContext) {
    Annotation annotation = new Annotation(new ArrayList<>());
    for (InheritanceResults inheritanceResults : trioInheritanceResults) {
      String variantContextKey = createVariantContextKey(variantContext);
      Map<String, Set<InheritanceMode>> variantInheritanceResultsMap = inheritanceResults
          .getVariantInheritanceResults();
      if (variantInheritanceResultsMap.containsKey(variantContextKey)) {
        Set<InheritanceMode> inheritanceModes = variantInheritanceResultsMap
            .get(variantContextKey);
        List<String> inheritanceModeAnnotations = annotation.getInheritanceMode();
        String annotationString = toAnnotationString(inheritanceResults.getGene(),
            inheritanceModes);
        if (annotationString != null) {
          inheritanceModeAnnotations.add(annotationString);
        }
      }
    }
    return annotation;
  }

  private static String toAnnotationString(String gene, Set<InheritanceMode> inheritanceModes) {
    Set<String> modes = new HashSet<>();
    Boolean isCompound = null;
    String annotationString = null;
    for (InheritanceMode inheritanceMode : inheritanceModes) {
      if (inheritanceMode.getMode() != InheritanceModeEnum.ANY) {
        modes.add(inheritanceMode.getMode().toString());
      }
      if (inheritanceMode.getIsCompound() != null) {
        isCompound = inheritanceMode.getIsCompound();
      }
    }
    if (!modes.isEmpty()) {
      String compound = "";
      if (Boolean.TRUE.equals(isCompound)) {
        compound = "1";
      } else if (isCompound != null) {
        compound = "0";
      }
      annotationString = String.format("%s|%s|%s", gene, String.join(",", modes), compound);
    }
    return annotationString;
  }
}
