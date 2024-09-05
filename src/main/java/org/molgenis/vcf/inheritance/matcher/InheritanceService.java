package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.inheritance.matcher.checker.ArCompoundChecker;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.utils.sample.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.inheritance.matcher.InheritanceMatcher.matchInheritance;
import static org.molgenis.vcf.utils.sample.mapper.PedToSamplesMapper.mapPedFileToPedigrees;

public class InheritanceService {

  private final Annotator annotator;
  private final List<Path> pedigreePaths;
  private final List<String> probands;
  private final VepMetadata vepMetadata;
  private ArCompoundChecker arCompoundChecker;
  private final PedigreeInheritanceChecker pedigreeInheritanceChecker;

  public InheritanceService(
          Annotator annotator, VepMetadata vepMetadata, PedigreeInheritanceChecker pedigreeInheritanceChecker, List<Path> pedigreePaths, List<String> probands) {
    this.annotator = requireNonNull(annotator);
    this.vepMetadata = requireNonNull(vepMetadata);
    this.pedigreeInheritanceChecker = requireNonNull(pedigreeInheritanceChecker);

      this.pedigreePaths = pedigreePaths;
      this.probands = probands;
  }

  public void run(VcfReader vcfReader, RecordWriter recordWriter) {
    this.arCompoundChecker = new ArCompoundChecker(vepMetadata);

    Map<String, Gene> knownGenes = new HashMap<>();
    Map<String, Pedigree> familyList;
    if (!pedigreePaths.isEmpty()) {
      familyList = mapPedFileToPedigrees(pedigreePaths);
    } else {
      familyList = createFamilyFromVcf(vcfReader.getFileHeader());
    }
    VCFHeader newHeader = annotator.annotateHeader(vcfReader.getFileHeader());
      recordWriter.writeHeader(newHeader);

      List<VcfRecord> vcfRecordList = new ArrayList<>();
      vcfReader.stream().forEach(vcfRecordList::add);
      Map<String, List<VcfRecord>> geneVariantMap = createGeneVariantMap(vepMetadata, knownGenes,
              vcfRecordList);
      vcfRecordList.stream().filter(record -> !record.getAlternateAlleles().isEmpty()).map(
          vcfRecord -> processSingleVariantcontext(probands, vepMetadata, familyList,
              geneVariantMap, vcfRecord)).forEach(recordWriter::add);

  }

  private VcfRecord processSingleVariantcontext(List<String> probands, VepMetadata vepMetadata,
      Map<String, Pedigree> pedigreeList,
      Map<String, List<VcfRecord>> geneVariantMap, VcfRecord vcfRecord) {
    Map<String, Inheritance> inheritanceMap = matchInheritanceForVariant(geneVariantMap,
            vcfRecord, pedigreeList, probands);
    Map<String, Annotation> annotationMap = matchInheritance(inheritanceMap,
        vcfRecord.getVcfRecordGenes());
    return annotator.annotateInheritance(vcfRecord, pedigreeList, annotationMap);
  }

  private Map<String, List<VcfRecord>> createGeneVariantMap(VepMetadata vepMetadata,
                                                            Map<String, Gene> knownGenes,
                                                            List<VcfRecord> vcfRecordList) {
    Map<String, List<VcfRecord>> geneVariantMap = new HashMap<>();
    for (VcfRecord vcfRecord : vcfRecordList) {
      VcfRecordGenes vcfRecordGenes = vcfRecord.getVcfRecordGenes(knownGenes);
      knownGenes.putAll(vcfRecordGenes.getGenes());
      for (Gene gene : vcfRecordGenes.getGenes().values()) {
        List<VcfRecord> geneVariantList;
        if (geneVariantMap.containsKey(gene.getId())) {
          geneVariantList = geneVariantMap.get(gene.getId());
        } else {
          geneVariantList = new ArrayList<>();
        }
        geneVariantList.add(vcfRecord);
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
      Map<String, List<VcfRecord>> geneVariantMap,
      VcfRecord record, Map<String, Pedigree> familyList,
      List<String> probands) {
    Map<String, Inheritance> result = new HashMap<>();
    for (Pedigree family : familyList.values()) {
      for (Sample sample : family.getMembers().values()) {
        if (probands.contains(sample.getPerson().getIndividualId()) || (probands.isEmpty()
                && sample.getPerson().getAffectedStatus() == AffectedStatus.AFFECTED)) {
          result.put(sample.getPerson().getIndividualId(),
                  calculateInheritanceForFamily(geneVariantMap, record, family,
                          sample));
        }
      }
    }
    return result;
  }

  private Inheritance calculateInheritanceForFamily(
    Map<String, List<VcfRecord>> geneVariantMap,
    VcfRecord record, Pedigree family,
    Sample sample) {
    return pedigreeInheritanceChecker.calculatePedigreeInheritance(geneVariantMap, record, sample, family, arCompoundChecker);
  }

}
