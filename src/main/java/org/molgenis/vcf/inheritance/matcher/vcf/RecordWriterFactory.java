package org.molgenis.vcf.inheritance.matcher.vcf;

import org.molgenis.vcf.inheritance.matcher.model.Settings;

public interface RecordWriterFactory {
  RecordWriter create(Settings settings);
}
