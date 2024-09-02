package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link VCFFileReader} wrapper that works with nested metadata and data (e.g. CSQ INFO fields).
 */
public class VcfReader implements AutoCloseable {

    private final VCFFileReader vcfFileReader;

    public VcfReader(VCFFileReader vcfFileReader) {
        this.vcfFileReader = requireNonNull(vcfFileReader);
    }

    public Stream<VcfRecord> stream() {
        return StreamSupport.stream(vcfFileReader.spliterator(), false).map(VcfRecord::new);
    }

    public Stream<VcfRecord> filteredStream(Set<String> classes) {
        //FIXME: filter on classes
        return StreamSupport.stream(vcfFileReader.spliterator(), false).filter(variantContext->{return variantContext != null;}).map(VcfRecord::new);
    }

    public Stream<VcfRecord> filteredStream(Set<String> classes, String geneId) {
        //FIXME: filter on classes and geneId
        return StreamSupport.stream(vcfFileReader.spliterator(), false).filter(variantContext->{return variantContext != null;}).map(VcfRecord::new);
    }

    @Override
    public void close() {
        vcfFileReader.close();
    }
}
