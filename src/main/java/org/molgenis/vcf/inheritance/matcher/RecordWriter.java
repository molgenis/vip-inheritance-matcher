package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.vcf.VCFHeader;

import java.io.Closeable;

public interface RecordWriter {
    void add(VcfRecord vcfRecord);
    void writeHeader(VCFHeader vcfHeader);
    void close();
}
