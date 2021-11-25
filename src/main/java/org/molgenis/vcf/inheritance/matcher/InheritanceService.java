package org.molgenis.vcf.inheritance.matcher;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.molgenis.vcf.inheritance.matcher.InheritanceMatcher.matchInheritance;
import static org.molgenis.vcf.inheritance.matcher.PedToSamplesMapper.mapPedFileToPersons;
import static org.molgenis.vcf.inheritance.matcher.checker.DeNovoChecker.checkDeNovo;

import htsjdk.variant.variantcontext.Allele;
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
import org.molgenis.vcf.inheritance.matcher.checker.XldChecker;
import org.molgenis.vcf.inheritance.matcher.checker.XlrChecker;
import org.molgenis.vcf.inheritance.matcher.model.AffectedStatus;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.Individual;
import org.molgenis.vcf.inheritance.matcher.model.Pedigree;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.Inheritance;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.Settings;
import org.molgenis.vcf.inheritance.matcher.model.Sex;
import org.springframework.stereotype.Component;

@Component
public class InheritanceService {

  ArChecker arChecker = new ArChecker();
  XldChecker xldChecker = new XldChecker();
  XlrChecker xlrChecker = new XlrChecker();
  private AdNonPenetranceChecker adNonPenetranceChecker;
  private ArCompoundChecker arCompoundChecker;
  private final Annotator annotator;

  public InheritanceService(
      Annotator annotator) {
    this.annotator = annotator;
  }

  public void run(Settings settings) {
    Path inputVcf = settings.getInputVcfPath();
    List<Path> pedigreePaths = settings.getInputPedPaths();
    List<String> probands = settings.getProbands();
    VCFFileReader vcfFileReader = createReader(inputVcf);

    VepMapper vepMapper = new VepMapper(vcfFileReader);
    this.adNonPenetranceChecker = new AdNonPenetranceChecker(vepMapper);
    this.arCompoundChecker = new ArCompoundChecker(vepMapper);

    Map<String, Gene> knownGenes = new HashMap<>();
    Map<String, Pedigree> familyList;
    if (!pedigreePaths.isEmpty()) {
      familyList = mapPedFileToPersons(
          pedigreePaths);
    } else {
      familyList = createFamilyFromVcf(vcfFileReader.getFileHeader());
    }
    VCFHeader newHeader = annotator.annotateHeader(vcfFileReader.getFileHeader());
    try (VariantContextWriter writer = createVcfWriter(settings.getOutputPath(),
        settings.isOverwrite())) {
      writer.writeHeader(newHeader);

      List<VariantContext> variantContextList = new ArrayList<>();
      vcfFileReader.forEach(variantContextList::add);
      Map<String, List<VariantContext>> geneVariantMap = createGeneVariantMap(vepMapper, knownGenes, variantContextList);
      variantContextList.stream().map(
          vc -> processSingleVariantcontext(probands, vepMapper, familyList,
              geneVariantMap, vc)).forEach(writer::add);
    } catch (IOException ioException) {
      throw new UncheckedIOException(ioException);
    }
  }

  private VariantContext processSingleVariantcontext(List<String> probands, VepMapper vepMapper, Map<String, Pedigree> pedigreeList,
      Map<String, List<VariantContext>> geneVariantMap, VariantContext vc) {
    Map<String, Inheritance> inheritanceMap = matchInheritanceForVariant(geneVariantMap,
        vc, pedigreeList, probands);
    Map<String, Annotation> annotationMap = matchInheritance(inheritanceMap,
        vepMapper.getGenes(vc).values());
    return annotator.annotateInheritance(vc, pedigreeList, annotationMap);
  }

  private Map<String, List<VariantContext>> createGeneVariantMap(VepMapper vepMapper,
      Map<String, Gene> knownGenes,
      List<VariantContext> variantContextList) {
    Map<String, List<VariantContext>> geneVariantMap = new HashMap<>();
    for (VariantContext vc : variantContextList) {
      Map<String, Gene> genes = vepMapper.getGenes(vc, knownGenes);
      knownGenes.putAll(genes);
      for (Gene gene : genes.values()) {
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
      Individual individual = Individual.builder().familyId(sampleName).id(sampleName).paternalId("")
          .maternalId("").proband(true).sex(Sex.UNKNOWN)
          .affectedStatus(AffectedStatus.AFFECTED).build();
      familyList.put(sampleName, new Pedigree(sampleName, singletonMap(sampleName, individual)));
    }
    return familyList;
  }

  private Map<String, Inheritance> matchInheritanceForVariant(
      Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Map<String, Pedigree> familyList,
      List<String> probands) {
    Map<String, Inheritance> result = new HashMap<>();
    for (Pedigree family : familyList.values()) {
      result.putAll(
          matchInheritanceForFamily(geneVariantMap, variantContext, family,
              probands));
    }
    return result;
  }

  private Map<String, Inheritance> matchInheritanceForFamily(
      Map<String, List<VariantContext>> geneVariantMap,
      VariantContext variantContext, Pedigree family,
      List<String> probands) {
    Map<String, Inheritance> result = new HashMap<>();
    Inheritance inheritance = Inheritance.builder().build();

    if (familyContains(probands, family) || probands.isEmpty()) {
      checkAr(geneVariantMap, variantContext, family, inheritance);
      checkAd(variantContext, family, inheritance);
      checkXl(variantContext, family, inheritance);
    }
    for (Individual individual : family.getMembers().values()) {
      if (probands.contains(individual.getId()) || (probands.isEmpty()
          && individual.getAffectedStatus() == AffectedStatus.AFFECTED)) {
        inheritance.setDenovo(checkDeNovo(variantContext, family, individual));
        result.put(individual.getId(), inheritance);
      }
    }
    return result;
  }

  private void checkXl(VariantContext variantContext, Pedigree family,
      Inheritance inheritance) {
    if (xldChecker.check(variantContext, family)) {
      inheritance.addSubInheritanceMode(SubInheritanceMode.XLD);
      inheritance.addInheritanceMode(InheritanceMode.XLD);
      inheritance.addInheritanceMode(InheritanceMode.XL);
    }
    if (xlrChecker.check(variantContext, family)) {
      inheritance.addSubInheritanceMode(SubInheritanceMode.XLR);
      inheritance.addInheritanceMode(InheritanceMode.XLR);
      inheritance.addInheritanceMode(InheritanceMode.XL);
    }
  }

  private void checkAd(VariantContext variantContext, Pedigree family,
      Inheritance inheritance) {
    if (AdChecker.check(variantContext, family)) {
      inheritance.addInheritanceMode(InheritanceMode.AD);
    } else {
      if (adNonPenetranceChecker.check(variantContext, family)) {
        inheritance.addSubInheritanceMode(SubInheritanceMode.AD_IP);
        inheritance.addInheritanceMode(InheritanceMode.AD);
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
        inheritance.addSubInheritanceMode(SubInheritanceMode.AR_C);
        inheritance.addInheritanceMode(InheritanceMode.AR);
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

  private boolean familyContains(List<String> probands, Pedigree family) {
    for (String proband : probands) {
      if (family.getMembers().keySet().contains(proband)) {
        return true;
      }
    }
    return false;
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
