package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.inheritance.matcher.VariantUtils.isMendelianViolation;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.jannovar.JannovarService;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceResults;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.model.Trio;
import org.molgenis.vcf.inheritance.matcher.ped.PedigreeMapper;
import org.springframework.stereotype.Component;

@Component
public class InheritanceMatcher {

  private final InheritanceService inheritanceService;
  private final SampleAnnotator sampleAnnotator;

  public InheritanceMatcher(JannovarService jannovarService, SampleAnnotator sampleAnnotator) {
    this.inheritanceService = requireNonNull(jannovarService);
    this.sampleAnnotator = requireNonNull(sampleAnnotator);
  }

  public void match(Settings settings) {
    try (VcfReader vcfReader = createReader(settings.getInputVcfPath())) {
      try(VariantContextWriter writer = createVcfWriter(settings.getOutputPath(),
          settings.isOverwrite())) {
        VCFHeader header = sampleAnnotator.addMetadata(vcfReader.getFileHeader(), settings.getInputPedPaths());
        writer.writeHeader(header);
        ArrayList<String> vcfSamples = header.getSampleNamesInOrder();
        Map<String, Trio> available = PedigreeMapper.map(settings.getInputPedPaths(), vcfSamples);
        Collection<Trio> trios = filterTrios(settings,
            available);
        Map<String, List<VariantContext>> variantsPerGene = vcfReader.getVariantsPerGene();

        Map<Trio, Set<InheritanceResults>> trioInheritanceResults = getInheritanceModes(
            trios, variantsPerGene);

        for (VariantContext variantContext : vcfReader) {
          Map<Trio, Annotation> annotations = new HashMap<>();
          for (Trio trio : trios) {
            Boolean isMendelianViolation = isMendelianViolation(trio, variantContext);
            annotations.put(trio, ResultToAnnotationMapper
                .map(trioInheritanceResults.get(trio), variantContext, isMendelianViolation));
          }
          writer.add(sampleAnnotator.annotate(variantContext, annotations));
        }
      }
    }
  }

  private Collection<Trio> filterTrios(Settings settings, Map<String, Trio> available) {
    Collection<Trio> trios;
    if(settings.getProbands().isEmpty()){
      trios = available.values();
    }else{
      trios = new ArrayList<>();
      for(String proband: settings.getProbands()) {
        if(available.containsKey(proband)){
          trios.add(available.get(proband));
        }else{
          throw new IncompleteTrioException(proband);
        }
      }
    }
    return trios;
  }

  private Map<Trio, Set<InheritanceResults>> getInheritanceModes(Collection<Trio> trios,
      Map<String, List<VariantContext>> variantsPerGene) {
    Map<Trio, Set<InheritanceResults>> trioInheritanceResults = new HashMap<>();
    for (Trio trio : trios) {
      trioInheritanceResults.put(trio, inheritanceService.matchInheritance(variantsPerGene, trio));
    }
    return trioInheritanceResults;
  }

  private VcfReader createReader(Path vcfPath) {
    return new VcfReader(new VCFFileReader(vcfPath, false));
  }

  private static VariantContextWriter createVcfWriter(Path outputVcfPath, boolean overwrite) {
    if (overwrite) {
      try {
        Files.deleteIfExists(outputVcfPath);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else if (Files.exists(outputVcfPath)) {
      throw new IllegalArgumentException(
          format("cannot create '%s' because it already exists.", outputVcfPath));
    }

    return new VariantContextWriterBuilder()
        .clearOptions()
        .setOutputFile(outputVcfPath.toFile())
        .build();
  }
}
