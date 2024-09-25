package org.molgenis.vcf.inheritance.matcher.vcf;

import org.molgenis.vcf.inheritance.matcher.model.Settings;

public interface VcfReaderFactory {
    VcfReader create(Settings settings);
}
