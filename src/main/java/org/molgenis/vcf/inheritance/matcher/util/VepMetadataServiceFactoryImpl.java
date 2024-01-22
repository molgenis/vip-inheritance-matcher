package org.molgenis.vcf.inheritance.matcher.util;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.FieldMetadataServiceImpl;
import org.molgenis.vcf.utils.model.FieldMetadata;
import org.molgenis.vcf.utils.vep.VepMetadataService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Quirky class to enable reuse of {@link VepMetadataService} from vip-utils
 */
@Component
public class VepMetadataServiceFactoryImpl implements VepMetadataServiceFactory {

    @Override
    public FieldMetadataService create() {
        return new VepMetadataService(new EmptyFieldMetadataService());
    }

    /**
     * vip-inheritance-matcher does not require knowledge of custom VEP metadata
     */
    static class EmptyFieldMetadataService extends FieldMetadataServiceImpl {
        private static final String EMPTY_METADATA_JSON = """
                {
                  "format": {
                  },
                  "info": {
                    "CSQ": {
                      "nestedFields": {
                      }
                    }
                  }
                }
                """;

        @Override
        public FieldMetadata load(VCFInfoHeaderLine vcfInfoHeaderLine) {
            return this.load(new ByteArrayInputStream(EMPTY_METADATA_JSON.getBytes(UTF_8)), vcfInfoHeaderLine);
        }
    }
}
