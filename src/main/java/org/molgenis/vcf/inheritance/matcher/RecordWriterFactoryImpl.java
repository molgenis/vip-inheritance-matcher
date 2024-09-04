package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

@Component
public class RecordWriterFactoryImpl implements RecordWriterFactory{
    @Override
    public RecordWriter create(Settings settings) {

        VariantContextWriter vcfWriter = createVcfWriter(settings);
        return new RecordWriterImpl(vcfWriter);
    }

    private static VariantContextWriter createVcfWriter(Settings settings) {
        Path outputVcfPath = settings.getOutputPath();
        if (settings.isOverwrite()) {
            try {
                Files.deleteIfExists(outputVcfPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else if (Files.exists(outputVcfPath)) {
            throw new IllegalArgumentException(
                    format("cannot create '%s' because it already exists.", outputVcfPath));
        }

        return new VariantContextWriterBuilder()
                .clearOptions()
                .setOutputFile(outputVcfPath.toFile())
                .build();
    }
}
