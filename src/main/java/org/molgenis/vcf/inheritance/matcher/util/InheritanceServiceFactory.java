package org.molgenis.vcf.inheritance.matcher.util;

import org.molgenis.vcf.inheritance.matcher.InheritanceService;
import org.molgenis.vcf.inheritance.matcher.VepMapper;
import org.molgenis.vcf.inheritance.matcher.model.Settings;

public interface InheritanceServiceFactory {
    InheritanceService create(Settings settings, VepMapper vepMapper);
}
