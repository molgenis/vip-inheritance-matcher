package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.vcf.VCFFileReader;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.VepMetadata;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.util.VepMetadataServiceFactoryImpl;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class VcfReaderFactoryImpl implements VcfReaderFactory {
    private final VepMetadataServiceFactoryImpl vepMetadataServiceFactoryImpl;

    public VcfReaderFactoryImpl(VepMetadataServiceFactoryImpl vepMetadataServiceFactoryImpl) {
        this.vepMetadataServiceFactoryImpl = vepMetadataServiceFactoryImpl;
    }

    @Override
    public VcfReader create(Settings settings) {
        Path inputVcfPath = settings.getInputVcfPath();
        VCFFileReader vcfFileReader = new VCFFileReader(inputVcfPath.toFile(), false);
        VepMetadata vepMetadata = new VepMetadata(vcfFileReader.getFileHeader(), vepMetadataServiceFactoryImpl.create(settings));
        return new VcfReader(vcfFileReader, new VcfRecordFactoryImpl(vepMetadata), settings.getPathogenicClasses());
    }
}
