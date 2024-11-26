package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.FieldMetadataServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;

/**
 * Quirky class to enable reuse of {@link FieldMetadataService} from vip-utils
 */
@Component
public class VepMetadataServiceFactoryImpl implements VepMetadataServiceFactory {

    @Override
    public FieldMetadataService create() {
        File json;
        try {
            json = ResourceUtils.getFile("classpath:metadata.json");
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return new FieldMetadataServiceImpl(json);
    }
}
