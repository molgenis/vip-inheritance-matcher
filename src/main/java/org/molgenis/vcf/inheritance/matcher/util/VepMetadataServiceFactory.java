package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.utils.metadata.FieldMetadataService;

public interface VepMetadataServiceFactory {
    FieldMetadataService create();
}
