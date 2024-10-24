package org.molgenis.vcf.inheritance.matcher.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.*;
import org.molgenis.vcf.inheritance.matcher.model.Settings;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class InheritanceResultServiceFactoryImplTest {
    @Mock
    private Annotator annotator;
    @Mock
    private PedigreeInheritanceChecker pedigreeInheritanceChecker;

    private InheritanceServiceFactoryImpl inheritanceServiceFactoryImpl;
    @BeforeEach
    void setUp() {
        inheritanceServiceFactoryImpl = new InheritanceServiceFactoryImpl(annotator, pedigreeInheritanceChecker);
    }
    @Test
    void create() {
        InheritanceService inheritanceServiceFactory = inheritanceServiceFactoryImpl.create(Settings.builder().build());
        assertNotNull(inheritanceServiceFactory);
    }
}