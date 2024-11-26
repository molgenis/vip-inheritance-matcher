package org.molgenis.vcf.inheritance.matcher.vcf.meta;

import java.io.Serial;

import static java.lang.String.format;

public class MissingVepMetadataException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

  public MissingVepMetadataException(String field) {
            super(format("VEP metadata is missing in metadata json, vep id: '%s'.", field));
        }
}
