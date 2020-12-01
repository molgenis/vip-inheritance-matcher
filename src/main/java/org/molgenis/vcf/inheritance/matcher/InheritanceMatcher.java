package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;
import org.springframework.stereotype.Component;

@Component
public class InheritanceMatcher {

  private final Annotator annotator;
  private final GenmodInheritanceMapper genmodInheritanceMapper;
  private final GenmodCompoundMapper genmodCompoundMapper;

  InheritanceMatcher(Annotator annotator, GenmodInheritanceMapper genmodInheritanceMapper,
      GenmodCompoundMapper genmodCompoundMapper) {
    this.annotator = annotator;
    this.genmodInheritanceMapper = genmodInheritanceMapper;
    this.genmodCompoundMapper = genmodCompoundMapper;
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

  void mapAndMatch(Settings settings) {
    VCFFileReader fileReader = createReader(settings.getInputVcfPath());
    VariantContextWriter writer = createVcfWriter(settings.getOutputPath(), settings.isOverwrite());
    writer.writeHeader(new Annotator().annotateHeader(fileReader.getFileHeader()));
    VepMapper vepMapper = new VepMapper(fileReader);
    Map<String, Set<String>> variantGeneList = genmodCompoundMapper
        .createVariantGeneList(fileReader, vepMapper);
    Map<String, String> familyMap = PedUtils
        .map(settings.getInputPedPaths(), settings.getProbands());

    for (VariantContext vc : fileReader) {
      Map<String, Annotation> annotations = matchInheritance(vc, vepMapper, variantGeneList);
      vc = annotator.annotate(vc, annotations, familyMap);
      writer.add(vc);
    }
    writer.close();
  }

  Map<String, Annotation> matchInheritance(VariantContext vc,
      VepMapper vepMapper,
      Map<String, Set<String>> variantGeneList) {
    Map<String, Annotation> annotations = genmodInheritanceMapper.mapInheritance(vc);
    Map<String, Set<InheritanceModeEnum>> geneInheritanceMap = vepMapper.getGeneInheritanceMap(vc);
    for (Entry<String, Annotation> annotationEntry : annotations.entrySet()) {
      Annotation annotation = annotationEntry.getValue();
      String familyId = annotationEntry.getKey();
      List<String> result = new ArrayList<>();
      for (Entry<String, Set<InheritanceModeEnum>> entry : geneInheritanceMap.entrySet()) {
        if (matchInheritanceMode(entry, annotation, familyId, vc, variantGeneList)) {
          result.add(entry.getKey());
        }
      }
      annotation.setMatchingGenes(result);
    }
    return annotations;
  }

  private boolean matchInheritanceMode(
      Entry<String, Set<InheritanceModeEnum>> inheritanceModesForGene,
      Annotation annotation, String familyId,
      VariantContext vc, Map<String, Set<String>> variantGeneList) {
    boolean result = false;
    for (InheritanceMode inheritanceMode : annotation.getInheritanceModes()) {
      if (inheritanceModesForGene.getValue().contains(inheritanceMode.getInheritanceModeEnum())) {
        if (inheritanceMode.getSubInheritanceMode() == SubInheritanceMode.COMP) {
          if (matchCompound(inheritanceModesForGene, familyId, vc, variantGeneList)) {
            result = true;
          }
        } else {
          result = true;
        }
      }
    }
    return result;
  }

  private boolean matchCompound(Entry<String, Set<InheritanceModeEnum>> inheritanceModesForGene,
      String familyId, VariantContext vc, Map<String, Set<String>> variantGeneList) {
    String gene = inheritanceModesForGene.getKey();
    Map<String, String[]> compounds = genmodCompoundMapper.mapCompounds(vc);
    for (String compound : compounds.get(familyId)) {
      if (variantGeneList.containsKey(compound)) {
        if (variantGeneList.get(compound).contains(gene)) {
          return true;
        }
      } else {
        throw new UnknownVariantException(compound);
      }
    }
    return false;
  }

  private VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }
}
