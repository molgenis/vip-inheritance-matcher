package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.vcf.VCFFileReader;
import org.molgenis.vcf.inheritance.matcher.model.Settings;

import java.nio.file.Path;

public class VcfReaderFactoryImpl implements VcfReaderFactory{
    @Override
    public VcfReader create(Settings settings) {
        Path inputVcfPath = settings.getInputVcfPath();
        return new VcfReader(new VCFFileReader(inputVcfPath.toFile(), false));
    }
}
