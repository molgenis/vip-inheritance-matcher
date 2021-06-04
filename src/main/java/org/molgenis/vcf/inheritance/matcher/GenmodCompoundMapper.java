package org.molgenis.vcf.inheritance.matcher;

import static java.util.stream.Collectors.joining;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GenmodCompoundMapper {

  private static final String COMPOUNDS_INFO_FIELD = "Compounds";

  Map<String, Set<String>> createVariantGeneList(VCFFileReader fileReader,
      VepMapper vepMapper) {
    Map<String, Set<String>> result = new HashMap<>();
    for (VariantContext vc : fileReader) {
      String key = createGenmodVariantIdentifier(vc);
      Set<String> genes = vepMapper.getGenes(vc);
      result.put(key, genes);
    }
    return result;
  }

  private static final Pattern PATTERN_REPLACE = Pattern.compile("[<>:\\[\\]]");

  /**
   * package-private for testability.
   */
  static String createGenmodVariantIdentifier(VariantContext vc) {
    // Fix https://github.com/molgenis/vip-inheritance-matcher/issues/11
    return vc.getContig()
        + '_'
        + vc.getStart()
        + '_'
        + vc.getReference().getDisplayString() // do not PATTERN_REPLACE on reference
        + '_'
        + vc.getAlternateAlleles().stream()
        .map(altAllele -> PATTERN_REPLACE.matcher(altAllele.getDisplayString()).replaceAll(""))
        .collect(joining(","));
  }


  //roundabout way of getting the actual string from the infofield, which is malformed in case of multi allelics
  //comma's are added in the created key, which are the list sepearators in a VCF INFO field
  //e.g. FAM001:1_1234567_G_A,T|1_1234568_C_T
  Map<String, String[]> mapCompounds(VariantContext vc) {
    String input = String.join(",",
        vc.getAttributeAsStringList(COMPOUNDS_INFO_FIELD, ""));
    Pattern p = Pattern.compile("(\\w++:)+");
    Matcher m = p.matcher(input);
    Map<String, Integer> keys = new HashMap<>();
    m.results().forEach(result ->
        keys.put(result.group(), input.indexOf(result.group())));
    List<Entry<String, Integer>> sorted = keys.entrySet().stream()
        .sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
    Map<String, String[]> compoundMap = new HashMap<>();
    int previousIndex = 0;
    Integer newIndex = -1;
    for (Entry<String, Integer> entry : sorted) {
      newIndex = entry.getValue();
      if (newIndex != 0) {
        String compoundValue = input.substring(previousIndex, newIndex);
        mapCompoundInfo(compoundValue, compoundMap);
        previousIndex = newIndex;
      }
    }
    mapCompoundInfo(input.substring(previousIndex), compoundMap);
    return compoundMap;
  }

  private void mapCompoundInfo(String compoundValue, Map<String, String[]> compoundMap) {
    compoundValue = compoundValue.replaceAll(",$", "");
    String[] split = compoundValue.split(":");
    if (split.length == 2) {
      String family = split[0];
      String[] compounds = split[1].split("\\|");
      compoundMap.put(family, compounds);
    } else {
      throw new UnexpectedValueFormatException(COMPOUNDS_INFO_FIELD);
    }
  }
}
