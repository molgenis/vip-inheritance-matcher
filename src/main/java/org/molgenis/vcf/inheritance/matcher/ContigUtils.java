package org.molgenis.vcf.inheritance.matcher;

import static org.molgenis.vcf.inheritance.matcher.ContigUtils.ContigType.AUTOSOME;
import static org.molgenis.vcf.inheritance.matcher.ContigUtils.ContigType.CHROMOSOME_MT;
import static org.molgenis.vcf.inheritance.matcher.ContigUtils.ContigType.CHROMOSOME_X;
import static org.molgenis.vcf.inheritance.matcher.ContigUtils.ContigType.CHROMOSOME_Y;
import static org.molgenis.vcf.inheritance.matcher.ContigUtils.ContigType.OTHER;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ContigUtils {

  private static final Map<String, ContigType> CONTIG_TYPE_MAP;
  private static final Pattern PATTERN_AUTOSOME = Pattern.compile("chr\\d+_.+");
  private static final Pattern PATTERN_CHROMOSOME_X = Pattern.compile("chrX_.+");
  private static final Pattern PATTERN_CHROMOSOME_Y = Pattern.compile("chrY_.+");

  public enum ContigType {
    AUTOSOME,
    CHROMOSOME_X,
    CHROMOSOME_Y,
    CHROMOSOME_MT,
    /**
     * known contig types other than chromosomes such as decoys
     */
    OTHER,
    UNKNOWN
  }

  private ContigUtils() {
  }

  public static boolean isAutosome(String contigId) {
    ContigType contigType = CONTIG_TYPE_MAP.get(contigId);

    boolean autosome;
    if (contigType != null) {
      autosome = contigType == AUTOSOME;
    } else {
      autosome = PATTERN_AUTOSOME.matcher(contigId).matches();
    }
    return autosome;
  }

  public static boolean isChromosomeX(String contigId) {
    ContigType contigType = CONTIG_TYPE_MAP.get(contigId);

    boolean chromosomeX;
    if (contigType != null) {
      chromosomeX = contigType == CHROMOSOME_X;
    } else {
      chromosomeX = PATTERN_CHROMOSOME_X.matcher(contigId).matches();
    }
    return chromosomeX;
  }

  public static boolean isChromosomeY(String contigId) {
    ContigType contigType = CONTIG_TYPE_MAP.get(contigId);

    boolean chromosomeY;
    if (contigType != null) {
      chromosomeY = contigType == CHROMOSOME_Y;
    } else {
      chromosomeY = PATTERN_CHROMOSOME_Y.matcher(contigId).matches();
    }
    return chromosomeY;
  }

  public static boolean isChromosomeMt(String contigId) {
    return CONTIG_TYPE_MAP.get(contigId) == CHROMOSOME_MT;
  }

  static {
    CONTIG_TYPE_MAP = new HashMap<>();
    for (int i = 1; i <= 22; ++i) {
      CONTIG_TYPE_MAP.put(String.valueOf(i), AUTOSOME);
      CONTIG_TYPE_MAP.put("chr" + i, AUTOSOME);
    }
    CONTIG_TYPE_MAP.put("X", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("chrX", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("Y", CHROMOSOME_Y);
    CONTIG_TYPE_MAP.put("chrY", CHROMOSOME_Y);
    CONTIG_TYPE_MAP.put("MT", CHROMOSOME_MT);
    CONTIG_TYPE_MAP.put("chrM", CHROMOSOME_MT);
    CONTIG_TYPE_MAP.put("chrEBV", OTHER);
    // https://www.ncbi.nlm.nih.gov/nuccore/NC_001422 (used as sequencing control)
    CONTIG_TYPE_MAP.put("NC_001422.1", OTHER);
    CONTIG_TYPE_MAP.put("CM000685.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("GL877877.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH159150.3", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH720451.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH720452.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH720453.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH720454.3", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH720455.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806587.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806588.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806589.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806590.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806591.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806592.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806593.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806594.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806595.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806596.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806597.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806598.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806599.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806600.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806601.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806602.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("JH806603.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("KB021648.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NC_000023.10", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003571064.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871103.3", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871098.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871099.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871100.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871101.3", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_003871102.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070877.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070878.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070879.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070880.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070881.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070882.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070883.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070884.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070885.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070886.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070887.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070888.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070889.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070890.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070891.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070892.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004070893.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_004166866.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("CM000685.2", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NC_000023.11", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("ML143381.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_021160027.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("ML143382.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_021160028.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("ML143383.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_021160029.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("ML143384.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_021160030.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("ML143385.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_021160031.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("KV766199.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NW_017363820.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("chrX_KV766199v1_alt", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("KI270880.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NT_187634.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("chrX_KI270880v1_alt", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("KI270881.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NT_187635.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("chrX_KI270881v1_alt", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("KI270913.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("NT_187667.1", CHROMOSOME_X);
    CONTIG_TYPE_MAP.put("chrX_KI270913v1_alt", CHROMOSOME_X);
  }
}
