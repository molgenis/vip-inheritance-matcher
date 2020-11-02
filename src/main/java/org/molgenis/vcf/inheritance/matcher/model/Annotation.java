package org.molgenis.vcf.inheritance.matcher.model;

import java.util.List;
import lombok.Data;
import lombok.NonNull;
import org.springframework.lang.Nullable;

@Data
public class Annotation {
  @NonNull List<String> inheritanceMode;
  @Nullable String mendelianViolation;
}
