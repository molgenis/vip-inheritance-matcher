package org.molgenis.vcf.inheritance.matcher.model;

import lombok.NonNull;

import java.util.Set;

public record GeneInfo(String geneId, String symbolSource, Set<InheritanceMode> inheritanceModes) implements Comparable<GeneInfo> {
    public int compareTo(@NonNull GeneInfo geneInfo) {
        return this.geneId.compareTo(geneInfo.geneId);
    }
}
