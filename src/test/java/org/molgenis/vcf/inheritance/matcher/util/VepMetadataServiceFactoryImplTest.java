package org.molgenis.vcf.inheritance.matcher.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.model.Settings;

import java.nio.file.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VepMetadataServiceFactoryImplTest {
    private VepMetadataServiceFactoryImpl vepMetadataServiceFactoryImpl;

    @BeforeEach
    void setUp() {
        vepMetadataServiceFactoryImpl = new VepMetadataServiceFactoryImpl();
    }

    @Test
    void create() {
        Settings settings = mock(Settings.class);
        when(settings.getMetadataPath()).thenReturn(Path.of("TEST"));
        // test that no exception is thrown
        vepMetadataServiceFactoryImpl.create(settings);
    }
}