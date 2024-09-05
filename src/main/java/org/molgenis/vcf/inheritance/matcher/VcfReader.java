package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link VCFFileReader} wrapper that works with nested metadata and data (e.g. CSQ INFO fields).
 */
public class VcfReader implements AutoCloseable {

    private final VCFFileReader vcfFileReader;
    private final VepMetadata vepMetadata;
    private final Set<String> pathogenicClasses;

    public VcfReader(VCFFileReader vcfFileReader, VepMetadata vepMetadata, Set<String> pathogenicClasses) {
        this.vcfFileReader = requireNonNull(vcfFileReader);
        this.vepMetadata = vepMetadata;
        this.pathogenicClasses = pathogenicClasses;
    }

    public Stream<VcfRecord> stream() {
        //FIXME
        return StreamSupport.stream(vcfFileReader.spliterator(), false).map(vc -> new VcfRecord(vc, vepMetadata, pathogenicClasses));
    }

    public Stream<VcfRecord> filteredStream(Set<String> classes) {
        //FIXME: filter on classes
        return StreamSupport.stream(vcfFileReader.spliterator(), false).filter(variantContext->{return variantContext != null;}).map(vc -> new VcfRecord(vc, vepMetadata, pathogenicClasses));
    }

    public Stream<VcfRecord> filteredStream(Set<String> classes, String geneId) {
        //FIXME: filter on classes and geneId
        return StreamSupport.stream(vcfFileReader.spliterator(), false).filter(variantContext->{return variantContext != null;}).map(vc -> new VcfRecord(vc, vepMetadata, pathogenicClasses));
    }

    @Override
    public void close() {
        vcfFileReader.close();
    }

    public VCFHeader getFileHeader() {
        return vcfFileReader.getFileHeader();
    }
}
