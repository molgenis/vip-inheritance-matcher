package org.molgenis.vcf.inheritance.matcher;

import java.util.HashMap;
import java.util.Map;
import org.molgenis.vcf.inheritance.matcher.model.Chromosome;

public class ChromosomeUtils {
  private static final Map<String, Chromosome> CHROMOSOME_SYNONYMS;

  static {
    CHROMOSOME_SYNONYMS = new HashMap<>();
    CHROMOSOME_SYNONYMS.put("X",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("chrX",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("CM000685.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("GL877877.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH159150.3",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH720451.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH720452.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH720453.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH720454.3",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH720455.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806587.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806588.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806589.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806590.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806591.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806592.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806593.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806594.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806595.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806596.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806597.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806598.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806599.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806600.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806601.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806602.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("JH806603.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("KB021648.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NC_000023.10",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003571064.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871103.3",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871098.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871099.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871100.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871101.3",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_003871102.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070877.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070878.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070879.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070880.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070881.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070882.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070883.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070884.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070885.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070886.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070887.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070888.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070889.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070890.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070891.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070892.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004070893.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_004166866.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("CM000685.2",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NC_000023.11",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("ML143381.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_021160027.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("ML143382.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_021160028.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("ML143383.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_021160029.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("ML143384.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_021160030.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("ML143385.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_021160031.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("KV766199.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NW_017363820.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("chrX_KV766199v1_alt",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("KI270880.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NT_187634.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("chrX_KI270880v1_alt",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("KI270881.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NT_187635.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("chrX_KI270881v1_alt",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("KI270913.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("NT_187667.1",Chromosome.X);
    CHROMOSOME_SYNONYMS.put("chrX_KI270913v1_alt",Chromosome.X);
  }

  private ChromosomeUtils() {
  }

  public static Chromosome mapChromosomeId(String id){
    return CHROMOSOME_SYNONYMS.get(id);
  }
}
