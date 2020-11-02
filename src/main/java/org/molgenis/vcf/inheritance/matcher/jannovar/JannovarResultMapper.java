package org.molgenis.vcf.inheritance.matcher.jannovar;

import static org.molgenis.vcf.inheritance.matcher.VariantUtils.createVariantContextKey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;

public class JannovarResultMapper {

  private JannovarResultMapper(){}

  public static Map<String, Set<InheritanceMode>> map(
      ImmutableMap<SubModeOfInheritance, ImmutableList<VariantContext>> compatibleInheritanceModes) {
    Map<String, Set<InheritanceMode>> variantInheritanceModes = new HashMap<>();
    for(Entry<SubModeOfInheritance, ImmutableList<VariantContext>> inheritanceResultEntry : compatibleInheritanceModes.entrySet()){
      InheritanceMode inheritanceMode = mapSubModeOfInheritance(inheritanceResultEntry.getKey());
      for(VariantContext variantContext : inheritanceResultEntry.getValue()){
        String variantKey = createVariantContextKey(variantContext);
        if(variantInheritanceModes.containsKey(variantKey)){
          Set<InheritanceMode> modes = variantInheritanceModes.get(variantKey);
          modes.add(inheritanceMode);
          variantInheritanceModes.put(variantKey, modes);
        }else{
          Set<InheritanceMode> inheritanceModes = new HashSet<>();
          inheritanceModes.add(inheritanceMode);
          variantInheritanceModes.put(variantKey, inheritanceModes);
        }
      }
    }
    return variantInheritanceModes;
  }

  private static InheritanceMode mapSubModeOfInheritance(SubModeOfInheritance subModeOfInheritance) {
    InheritanceMode.InheritanceModeBuilder inheritanceModeBuilder = InheritanceMode.builder();
      switch (subModeOfInheritance) {
        case AUTOSOMAL_DOMINANT:
          inheritanceModeBuilder.mode(InheritanceModeEnum.AD);
          break;
        case AUTOSOMAL_RECESSIVE_HOM_ALT:
          inheritanceModeBuilder.mode(InheritanceModeEnum.AR).isCompound(false);
          break;
        case AUTOSOMAL_RECESSIVE_COMP_HET:
          inheritanceModeBuilder.mode(InheritanceModeEnum.AR).isCompound(true);
          break;
        case X_DOMINANT:
          inheritanceModeBuilder.mode(InheritanceModeEnum.XD);
          break;
        case X_RECESSIVE_HOM_ALT:
          inheritanceModeBuilder.mode(InheritanceModeEnum.XR).isCompound(false);
          break;
        case X_RECESSIVE_COMP_HET:
          inheritanceModeBuilder.mode(InheritanceModeEnum.XR).isCompound(true);
          break;
        case MITOCHONDRIAL:
          inheritanceModeBuilder.mode(InheritanceModeEnum.MT);
          break;
        default:
          inheritanceModeBuilder.mode(InheritanceModeEnum.ANY);
          break;
      }
      return inheritanceModeBuilder.build();
    }
}
