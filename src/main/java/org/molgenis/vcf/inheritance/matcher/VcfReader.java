package org.molgenis.vcf.inheritance.matcher;

import static java.util.Objects.requireNonNull;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link VCFFileReader} wrapper that works with nested metadata and data (e.g. CSQ INFO fields).
 */
public class VcfReader implements AutoCloseable {

    private final VCFFileReader vcfFileReader;
    @Getter
    private final VcfRecordFactory vcfRecordFactory;
    private final Set<String> pathogenicClasses;

    public VcfReader(VCFFileReader vcfFileReader, VcfRecordFactory vcfRecordFactory, Set<String> pathogenicClasses) {
        this.vcfFileReader = requireNonNull(vcfFileReader);
        this.vcfRecordFactory = vcfRecordFactory;
        this.pathogenicClasses = pathogenicClasses;
    }

    public Stream<VariantRecord> stream() {
        return StreamSupport.stream(vcfFileReader.spliterator(), false).map(vc -> vcfRecordFactory.create(vc, pathogenicClasses));
    }

    @Override
    public void close() {
        vcfFileReader.close();
    }

    public VCFHeader getFileHeader() {
        return vcfFileReader.getFileHeader();
    }
}
