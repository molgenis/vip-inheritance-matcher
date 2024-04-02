package org.molgenis.vcf.inheritance.matcher.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.Annotator;
import org.molgenis.vcf.inheritance.matcher.PedigreeInheritanceChecker;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InheritanceServiceFactoryImplTest {
    @Mock
    private Annotator annotator;
    @Mock
    private VepMetadataServiceFactory vepMetadataServiceFactory;
    @Mock
    private PedigreeInheritanceChecker pedigreeInheritanceChecker;
    private InheritanceServiceFactoryImpl inheritanceServiceFactoryImpl;
    @BeforeEach
    void setUp() {
        inheritanceServiceFactoryImpl = new InheritanceServiceFactoryImpl(annotator, vepMetadataServiceFactory, pedigreeInheritanceChecker);
    }
    @Test
    void create() {
        inheritanceServiceFactoryImpl.create();
        verify(vepMetadataServiceFactory).create();
    }
}