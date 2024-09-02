package org.molgenis.vcf.inheritance.matcher;

import org.molgenis.vcf.inheritance.matcher.model.Settings;

public interface VcfReaderFactory {
    VcfReader create(Settings settings);
}
