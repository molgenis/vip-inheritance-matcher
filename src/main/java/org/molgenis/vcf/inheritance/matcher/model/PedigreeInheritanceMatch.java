package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Value;

@Value
public class PedigreeInheritanceMatch {
    InheritanceMode inheritanceMode;
    boolean isUncertain;
}
