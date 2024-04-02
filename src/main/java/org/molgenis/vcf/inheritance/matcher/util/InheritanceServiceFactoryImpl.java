package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.inheritance.matcher.Annotator;
import org.molgenis.vcf.inheritance.matcher.InheritanceService;
import org.molgenis.vcf.inheritance.matcher.PedigreeInheritanceChecker;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Quirky class to enable use of local {@link org.molgenis.vcf.utils.vep.VepMetadataService}
 */
@Component
public class InheritanceServiceFactoryImpl implements InheritanceServiceFactory {
    private final Annotator annotator;
    private final VepMetadataServiceFactory vepMetadataServiceFactory;
    private final PedigreeInheritanceChecker pedigreeInheritanceChecker;

    public InheritanceServiceFactoryImpl(Annotator annotator, VepMetadataServiceFactory vepMetadataServiceFactory, PedigreeInheritanceChecker pedigreeInheritanceChecker) {
        this.annotator = requireNonNull(annotator);
        this.vepMetadataServiceFactory = requireNonNull(vepMetadataServiceFactory);
        this.pedigreeInheritanceChecker = requireNonNull(pedigreeInheritanceChecker);
    }

    @Override
    public InheritanceService create() {
        return new InheritanceService(annotator, vepMetadataServiceFactory.create(), pedigreeInheritanceChecker);
    }
}
