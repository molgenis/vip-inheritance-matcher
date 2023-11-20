package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.molgenis.vcf.inheritance.matcher.InheritanceMatcher.matchInheritance;
import static org.molgenis.vcf.utils.sample.mapper.PedToSamplesMapper.mapPedFileToPedigrees;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.molgenis.vcf.inheritance.matcher.checker.AdChecker;
import org.molgenis.vcf.inheritance.matcher.checker.AdNonPenetranceChecker;
import org.molgenis.vcf.inheritance.matcher.checker.ArChecker;
import org.molgenis.vcf.inheritance.matcher.checker.ArCompoundChecker;
import org.molgenis.vcf.inheritance.matcher.checker.DeNovoChecker;
import org.molgenis.vcf.inheritance.matcher.checker.XldChecker;
import org.molgenis.vcf.inheritance.matcher.checker.XlrChecker;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.inheritance.matcher.util.InheritanceUtils;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Pedigree;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class InheritanceService {

  private final FieldMetadataService fieldMetadataService;
  private final Annotator annotator;
  private ArCompoundChecker arCompoundChecker;

  ArChecker arChecker = new ArChecker();
  XldChecker xldChecker = new XldChecker();
  XlrChecker xlrChecker = new XlrChecker();
  DeNovoChecker deNovoChecker = new DeNovoChecker();
  public InheritanceService(
      Annotator annotator,   @Qualifier("vepMetadataService")
      FieldMetadataService fieldMetadataService) {
    this.annotator = annotator;
    this.fieldMetadataService = fieldMetadataService;
  }

  public void run(Settings settings) {
    Path inputVcf = settings.getInputVcfPath();
    List<Path> pedigreePaths = settings.getInputPedPaths();
    List<String> probands = settings.getProbands();
    VCFFileReader vcfFileReader = createReader(inputVcf);

    VepMapper vepMapper = new VepMapper(vcfFileReader, fieldMetadataService);
    this.arCompoundChecker = new ArCompoundChecker(vepMapper);

    Map<String, Gene> knownGenes = new HashMap<>();
    Map<String, Pedigree> familyList;
    if (!pedigreePaths.isEmpty()) {
      familyList = mapPedFileToPedigrees(pedigreePaths);
    } else {
      familyList = createFamilyFromVcf(vcfFileReader.getFileHeader());
    }
    VCFHeader newHeader = annotator.annotateHeader(vcfFileReader.getFileHeader());
    try (VariantContextWriter writer = createVcfWriter(settings.getOutputPath(),
        settings.isOverwrite())) {
      writer.writeHeader(newHeader);

      List<VariantContext> variantContextList = new ArrayList<>();
      vcfFileReader.forEach(variantContextList::add);
      Map<String, List<VariantContext>> geneVariantMap = createGeneVariantMap(vepMapper, knownGenes,
          variantContextList);
      variantContextList.stream().filter(vc -> vc.getAlternateAlleles().size() == 1).map(
          vc -> processSingleVariantcontext(probands, vepMapper, familyList,
              geneVariantMap, vc)).forEach(writer::add);
    } catch (IOException ioException) {
      throw new UncheckedIOException(ioException);
    }
  }

  private VariantContext processSingleVariantcontext(List<String> probands, VepMapper vepMapper,
      Map<String, Pedigree> pedigreeList,
      Map<String, List<VariantContext>> geneVariantMap, VariantContext vc) {
    Map<String, Inheritance> inheritanceMap = matchInheritanceForVariant(geneVariantMap,
        vc, pedigreeList, probands);
    Map<String, Annotation> annotationMap = matchInheritance(inheritanceMap,
        vepMapper.getGenes(vc));
    return annotator.annotateInheritance(vc, pedigreeList, annotationMap);
  }

  private Map<String, List<VariantContext>> createGeneVariantMap(VepMapper vepMapper,
      Map<String, Gene> knownGenes,
      List<VariantContext> variantContextList) {
    Map<String, List<VariantContext>> geneVariantMap = new HashMap<>();
    for (VariantContext vc : variantContextList) {
      VariantContextGenes variantContextGenes = vepMapper.getGenes(vc, knownGenes);
      knownGenes.putAll(variantContextGenes.getGenes());
      for (Gene gene : variantContextGenes.getGenes().values()) {
        List<VariantContext> geneVariantList;
        if (geneVariantMap.containsKey(gene.getId())) {
          geneVariantList = geneVariantMap.get(gene.getId());
        } else {
          geneVariantList = new ArrayList<>();
        }
        geneVariantList.add(vc);
        geneVariantMap.put(gene.getId(), geneVariantList);
      }
    }
    return geneVariantMap;
  }

  private Map<String, Pedigree> createFamilyFromVcf(VCFHeader fileHeader) {
    Map<String, Pedigree> familyList = new HashMap<>();
    ArrayList<String> sampleNames = fileHeader.getSampleNamesInOrder();
    for (String sampleName : sampleNames) {
      //no ped: unknown Sex, assume affected, no relatives, therefor the sampleId can be used as familyId
      Person person = Person.builder().familyId(sampleName).individualId(sampleName)
          .paternalId("")
          .maternalId("").sex(Sex.UNKNOWN)
          .affectedStatus(AffectedStatus.AFFECTED).build();
      Sample sample = Sample.builder().person(person).proband(true).build();
      familyList.put(sampleName, Pedigree.builder()
          .id(sampleName).members(singletonMap(sampleName, sample)).build());
    }
    return familyList;
  }

  private Map<String, Inheritance> matchInheritanceForVariant(
      Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Map<String, Pedigree> familyList,
      List<String> probands) {
    Map<String, Inheritance> result = new HashMap<>();
    for (Pedigree family : familyList.values()) {
      for (Sample sample : family.getMembers().values()) {
        if (probands.contains(sample.getPerson().getIndividualId()) || (probands.isEmpty()
                && sample.getPerson().getAffectedStatus() == AffectedStatus.AFFECTED)) {
          result.put(sample.getPerson().getIndividualId(),
                  matchInheritanceForSample(geneVariantMap, variantContext, family,
                          sample));
        }
      }
    }
    return result;
  }

  private Inheritance matchInheritanceForSample(
    Map<String, List<VariantContext>> geneVariantMap,
    VariantContext variantContext, Pedigree family,
    Sample sample) {
    Pedigree filteredFamily = InheritanceUtils.filterBloodRelatives(family, sample);
    Inheritance inheritance = Inheritance.builder().build();
    checkAr(geneVariantMap, variantContext, filteredFamily, inheritance);
    checkAd(variantContext, filteredFamily, inheritance);
    checkXl(variantContext, filteredFamily, inheritance);
    inheritance.setDenovo(deNovoChecker.checkDeNovo(variantContext, filteredFamily, sample));
    inheritance.setFamilyWithMissingGT(checkMissingGTs(filteredFamily, variantContext));

    return inheritance;
  }

  private boolean checkMissingGTs(Pedigree filteredFamily,VariantContext variantContext) {
    return filteredFamily.getMembers().values().stream().anyMatch(sample -> {
      Genotype gt = variantContext.getGenotype(sample.getPerson().getIndividualId());
      return gt == null || !gt.isCalled();
    });
  }

  private void checkXl(VariantContext variantContext, Pedigree family,
      Inheritance inheritance) {
    if (xldChecker.check(variantContext, family)) {
      inheritance.addInheritanceMode(InheritanceMode.XLD);
    }
    if (xlrChecker.check(variantContext, family)) {
      inheritance.addInheritanceMode(InheritanceMode.XLR);
    }
  }

  private void checkAd(VariantContext variantContext, Pedigree family,
      Inheritance inheritance) {
    if (AdChecker.check(variantContext, family)) {
      inheritance.addInheritanceMode(InheritanceMode.AD);
    } else {
      if (AdNonPenetranceChecker.check(variantContext, family)) {
        inheritance.addInheritanceMode(InheritanceMode.AD_IP);
      }
    }
  }

  private void checkAr(Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Pedigree family,
      Inheritance inheritance) {
    if (arChecker.check(variantContext, family)) {
      inheritance.addInheritanceMode(InheritanceMode.AR);
    } else {
      List<VariantContext> compounds = arCompoundChecker
          .check(geneVariantMap, variantContext, family);
      if (!compounds.isEmpty()) {
        inheritance.addInheritanceMode(InheritanceMode.AR_C);
        inheritance.setCompounds(compounds.stream().map(this::createKey).collect(
            Collectors.toSet()));
      }
    }
  }

  private String createKey(VariantContext compound) {
    return String.format("%s_%s_%s_%s", compound.getContig(), compound.getStart(),
        compound.getReference().getBaseString(),
        compound.getAlternateAlleles().stream().map(Allele::getBaseString)
            .collect(Collectors.joining("/")));
  }

  private static VCFFileReader createReader(Path vcfPath) {
    return new VCFFileReader(vcfPath, false);
  }

  private static VariantContextWriter createVcfWriter(Path outputVcfPath, boolean overwrite)
      throws IOException {
    if (overwrite) {
      Files.deleteIfExists(outputVcfPath);
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
