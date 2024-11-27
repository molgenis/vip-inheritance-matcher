package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.metadata.FieldMetadataServiceImpl;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Quirky class to enable reuse of {@link FieldMetadataService} from vip-utils
 */
@Component
public class VepMetadataServiceFactoryImpl implements VepMetadataServiceFactory {
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
    @SuppressWarnings("java:S5443")
    public FieldMetadataService create() {
        File json;
        try {
            Path path = Files.createTempFile("metadata", ".json");
            byte[] buf = EMPTY_METADATA_JSON.getBytes();
            Files.write(path, buf);
            json = path.toFile();
            json.deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new FieldMetadataServiceImpl(json);
    }
}
