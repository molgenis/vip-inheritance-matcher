package org.molgenis.vcf.inheritance.matcher.model;

import java.util.Set;

public record GeneInfo(String geneId, String symbolSource, Set<InheritanceMode> inheritanceModes)
    implements Comparable<GeneInfo> {

  @Override
  public int compareTo(GeneInfo geneInfo) {
    return this.geneId.compareTo(geneInfo.geneId);
  }
}
