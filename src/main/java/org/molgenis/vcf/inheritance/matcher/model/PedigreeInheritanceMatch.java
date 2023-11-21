package org.molgenis.vcf.inheritance.matcher.model;

import lombok.Builder;
import lombok.Value;

@Value
public class PedigreeInheritanceMatch {
    InheritanceMode inheritanceMode;
    boolean isUncertain;
}
