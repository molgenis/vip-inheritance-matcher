package org.molgenis.vcf.inheritance.matcher.util;

import htsjdk.variant.variantcontext.Allele;
import org.molgenis.vcf.inheritance.matcher.vcf.Genotype;
import org.molgenis.vcf.utils.sample.model.Sample;

public class InheritanceUtils {
    private InheritanceUtils() {
    }

    public static boolean hasParents(Sample sample) {
        return !(sample.getPerson().getMaternalId().isEmpty() || sample.getPerson().getMaternalId().equals("0")) &&
                !(sample.getPerson().getPaternalId().isEmpty() || sample.getPerson().getPaternalId().equals("0"));
    }

    public static boolean hasMissing(Genotype genotype) {
        return genotype == null || genotype.getAlleles().stream()
                .anyMatch(Allele::isNoCall);
    }
}
