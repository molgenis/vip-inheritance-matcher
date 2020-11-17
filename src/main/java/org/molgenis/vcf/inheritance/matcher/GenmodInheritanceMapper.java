package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.MappedInheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;
import org.springframework.stereotype.Component;

@Component
public class GenmodInheritanceMapper {

  private static final String INHERITANCE_INFO_FIELD = "GeneticModels";

  private static void mapInheritance(List<Annotation> annotations, String inheritanceValue) {
    List<MappedInheritanceMode> mappedInheritanceModes = new ArrayList<>();
    String[] inheritanceSplit = inheritanceValue.split(":");
    if (inheritanceSplit.length == 2) {
      String family = inheritanceSplit[0];
      String[] inheritanceModes = inheritanceSplit[1].split("\\|");
      for (String inheritanceMode : inheritanceModes) {
        mappedInheritanceModes.add(new GenmodInheritanceMapper().mapInheritance(inheritanceMode));
      }
      Set<InheritanceMode> inheritanceModeSet = new HashSet<>();
      boolean denovo = false;
      for (MappedInheritanceMode mappedInheritanceMode : mappedInheritanceModes) {
        if (mappedInheritanceMode.isDenovo()) {
          denovo = true;
        }
        inheritanceModeSet.add(mappedInheritanceMode.getInheritanceMode());
      }
      annotations.add(
          Annotation.builder().familyID(family).inheritanceMode(inheritanceModeSet)
              .denovo(denovo)
              .build());
    } else {
      throw new UnexpectedValueFormatException(INHERITANCE_INFO_FIELD);
    }
  }

  private static boolean isSampleInheritanceAnnotated(VariantContext vc) {
    return vc.hasAttribute(INHERITANCE_INFO_FIELD);
  }

  List<Annotation> mapInheritance(VariantContext vc) {
    List<Annotation> annotations = new ArrayList<>();
    if (isSampleInheritanceAnnotated(vc)) {
      List<String> inheritanceValues = vc.getAttributeAsStringList(INHERITANCE_INFO_FIELD, "");
      for (String inheritanceValue : inheritanceValues) {
        mapInheritance(annotations, inheritanceValue);
      }
    }
    return annotations;
  }

  private MappedInheritanceMode mapInheritance(String inheritanceModeString) {
    MappedInheritanceMode.MappedInheritanceModeBuilder mappedInheritanceModeBuilder = MappedInheritanceMode
        .builder();
    switch (inheritanceModeString) {
      case "AR_hom":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AR)
                .subInheritanceMode(SubInheritanceMode.HOM).build()).isDenovo(false);
        break;
      case "AR_hom_dn":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AR)
                .subInheritanceMode(SubInheritanceMode.HOM).build()).isDenovo(true);
        break;
      case "AR_comp":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AR)
                .subInheritanceMode(SubInheritanceMode.COMP).build()).isDenovo(false);
        break;
      case "AR_comp_dn":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AR)
                .subInheritanceMode(SubInheritanceMode.COMP).build()).isDenovo(true);
        break;
      case "AD":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AD).build())
            .isDenovo(false);
        break;
      case "AD_dn":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.AD).build())
            .isDenovo(true);
        break;
      case "XD":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.XD).build())
            .isDenovo(false);
        break;
      case "XD_dn":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.XD).build())
            .isDenovo(true);
        break;
      case "XR":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.XR).build())
            .isDenovo(false);
        break;
      case "XR_dn":
        mappedInheritanceModeBuilder.inheritanceMode(
            InheritanceMode.builder().inheritanceModeEnum(InheritanceModeEnum.XR).build())
            .isDenovo(true);
        break;
      default:
        throw new UnexpectedInheritanceModeException(inheritanceModeString);
    }
    return mappedInheritanceModeBuilder.build();
  }
}
