package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;

public interface RecordWriter {
    void add(VariantContext variantContext);
    void writeHeader(VCFHeader vcfHeader);
    void close();
}
