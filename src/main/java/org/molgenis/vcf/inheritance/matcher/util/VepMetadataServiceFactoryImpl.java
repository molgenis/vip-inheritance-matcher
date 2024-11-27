package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.FieldMetadataServiceImpl;
import org.springframework.stereotype.Component;

/**
 * Quirky class to enable reuse of {@link FieldMetadataService} from vip-utils
 */
@Component
public class VepMetadataServiceFactoryImpl implements VepMetadataServiceFactory {

    @Override
    @SuppressWarnings("java:S5443")
    public FieldMetadataService create(Settings settings) {
        return new FieldMetadataServiceImpl(settings.getMetadataPath().toFile());
    }
}
