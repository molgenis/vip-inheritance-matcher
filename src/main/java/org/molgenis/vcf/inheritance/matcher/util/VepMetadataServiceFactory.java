package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;

public interface VepMetadataServiceFactory {
    FieldMetadataService create(Settings settings);
}
