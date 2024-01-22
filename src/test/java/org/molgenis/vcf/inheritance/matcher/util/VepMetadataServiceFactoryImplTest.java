package org.molgenis.vcf.inheritance.matcher.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VepMetadataServiceFactoryImplTest {
    private VepMetadataServiceFactoryImpl vepMetadataServiceFactoryImpl;

    @BeforeEach
    void setUp() {
        vepMetadataServiceFactoryImpl = new VepMetadataServiceFactoryImpl();
    }

    @Test
    void create() {
        // test that no exception is thrown
        vepMetadataServiceFactoryImpl.create();
    }
}