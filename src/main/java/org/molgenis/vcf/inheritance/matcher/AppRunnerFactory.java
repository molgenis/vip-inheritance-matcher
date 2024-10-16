package org.molgenis.vcf.inheritance.matcher;


import org.molgenis.vcf.inheritance.matcher.model.Settings;

public interface AppRunnerFactory {

  AppRunner create(Settings settings);
}
