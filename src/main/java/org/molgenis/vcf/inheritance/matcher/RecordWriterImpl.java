package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;

import java.io.Closeable;

public class RecordWriterImpl implements RecordWriter, Closeable {

    private final VariantContextWriter vcfWriter;

    public RecordWriterImpl(VariantContextWriter vcfWriter) {
        this.vcfWriter = requireNonNull(vcfWriter);
    }

    @Override
    public void writeHeader(VCFHeader vcfHeader) {
        this.vcfWriter.writeHeader(vcfHeader);
    }

    @Override
    public void add(VariantContext vc) {
        this.vcfWriter.add(vc);
    }

    @Override
    public void close() {
        this.vcfWriter.close();
    }
}
