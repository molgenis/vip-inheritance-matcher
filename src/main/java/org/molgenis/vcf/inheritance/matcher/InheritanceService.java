package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.inheritance.matcher.checker.ArCompoundChecker;
import org.molgenis.vcf.inheritance.matcher.checker.DeNovoChecker;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.inheritance.matcher.vcf.RecordWriter;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfReader;
import org.molgenis.vcf.inheritance.matcher.vcf.VcfRecord;
import org.molgenis.vcf.utils.sample.model.*;

import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.inheritance.matcher.vcf.VariantContextUtils.getAltAlleles;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.*;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.FALSE;
import static org.molgenis.vcf.inheritance.matcher.model.MatchEnum.POTENTIAL;
import static org.molgenis.vcf.utils.sample.mapper.PedToSamplesMapper.mapPedFileToPedigrees;

public class InheritanceService {
    private final Annotator annotator;
    private final PedigreeInheritanceChecker pedigreeInheritanceChecker;
    private final List<Path> pedigreePaths;
    private final List<String> probands;
    private final DeNovoChecker deNovoChecker = new DeNovoChecker();
    private final ArCompoundChecker arCompoundChecker = new ArCompoundChecker();

    public InheritanceService(
            Annotator annotator, PedigreeInheritanceChecker pedigreeInheritanceChecker, List<Path> pedigreePaths, List<String> probands) {
        this.annotator = requireNonNull(annotator);
        this.pedigreeInheritanceChecker = requireNonNull(pedigreeInheritanceChecker);
        this.pedigreePaths = pedigreePaths;
        this.probands = probands;
    }

    public void run(VcfReader vcfReader, RecordWriter recordWriter) {

        Collection<Pedigree> pedigrees;
        if (!pedigreePaths.isEmpty()) {
            pedigrees = mapPedFileToPedigrees(pedigreePaths).values();
        } else {
            pedigrees = createFamilyFromVcf(vcfReader.getFileHeader());
        }

        VCFHeader newHeader = annotator.annotateHeader(vcfReader.getFileHeader());
        recordWriter.writeHeader(newHeader);

        Map<GeneInfo, Set<VcfRecord>> vcfRecordGeneInfoMap = new HashMap<>();
        List<VcfRecord> vcfRecords = vcfReader.stream().toList();

        for (VcfRecord vcfRecord : vcfRecords) {
            //Only perform matching if a pathogenic or vus allele is present
            if (!vcfRecord.pathogenicAlleles().isEmpty()) {
                addToVcfRecordMap(vcfRecord, vcfRecordGeneInfoMap);
            }
        }

        for (VcfRecord vcfRecord : vcfRecords) {
            matchVcfRecord(recordWriter, vcfRecord, pedigrees, vcfRecordGeneInfoMap);
        }
    }

    private void matchVcfRecord(RecordWriter recordWriter, VcfRecord vcfRecord, Collection<Pedigree> pedigrees, Map<GeneInfo, Set<VcfRecord>> vcfRecordGeneInfoMap) {
        Map<Pedigree, InheritanceResult> inheritanceResultMap = new HashMap<>();
        matchPedigree(vcfRecord, pedigrees, vcfRecordGeneInfoMap, inheritanceResultMap);

        VariantContext annotatedVc = annotator.annotateInheritance(vcfRecord, pedigrees, inheritanceResultMap, probands);
        recordWriter.add(annotatedVc);
    }

    private void matchPedigree(VcfRecord vcfRecord, Collection<Pedigree> pedigrees, Map<GeneInfo, Set<VcfRecord>> vcfRecordGeneInfoMap, Map<Pedigree, InheritanceResult> inheritanceResultMap) {
        for (Pedigree pedigree : pedigrees) {
            InheritanceResult inheritanceResult = InheritanceResult.builder().build();
            Set<Allele> altAllelesForPedigree = getAltAlleles(vcfRecord, pedigree);
            //Only perform matching if a family member with a pathogenic allele is present
            if (altAllelesForPedigree.stream().anyMatch(allele -> vcfRecord.pathogenicAlleles().contains(allele))) {
                addToVcfRecordMap(vcfRecord, vcfRecordGeneInfoMap);
                Set<InheritanceMode> modes = Set.of(AD, AR, XLD, XLR, MT, YL);
                modes.forEach(mode -> {
                    MatchEnum isMatch = pedigreeInheritanceChecker.check(vcfRecord, pedigree, mode);
                    if (isMatch != FALSE) {
                        inheritanceResult.addInheritanceMode(new PedigreeInheritanceMatch(mode, isMatch == POTENTIAL));
                    }
                });
                if (inheritanceResult.getPedigreeInheritanceMatches().stream().noneMatch(match -> match.inheritanceMode() == AD)) {
                    MatchEnum isAdIpMatch = pedigreeInheritanceChecker.check(vcfRecord, pedigree, AD_IP);
                    if (isAdIpMatch != FALSE) {
                        inheritanceResult.addInheritanceMode(new PedigreeInheritanceMatch(AD_IP, isAdIpMatch == POTENTIAL));
                    }
                }

                matchCompounds(vcfRecord, vcfRecordGeneInfoMap, pedigree, inheritanceResult);

                Map<Sample, MatchEnum> denovoResult = new HashMap<>();
                pedigree.getMembers().values().stream().filter(sample -> probands.isEmpty() || probands.contains(sample.getPerson().getIndividualId())).forEach(proband ->
                        denovoResult.put(proband, deNovoChecker.checkDeNovo(vcfRecord, proband)));
                inheritanceResult.setDenovo(denovoResult);
                inheritanceResultMap.put(pedigree, inheritanceResult);
            }
        }
    }

    private void matchCompounds(VcfRecord vcfRecord, Map<GeneInfo, Set<VcfRecord>> vcfRecordGeneInfoMap, Pedigree pedigree, InheritanceResult inheritanceResult) {
        Map<GeneInfo, Set<CompoundCheckResult>> compoundsMap = arCompoundChecker.check(vcfRecordGeneInfoMap, vcfRecord, pedigree);
        if (!isEmpty(compoundsMap)) {
            inheritanceResult.setCompounds(compoundsMap);
            boolean isCertain = false;
            for (Set<CompoundCheckResult> compoundCheckResult : compoundsMap.values()) {
                isCertain = compoundCheckResult.stream().anyMatch(CompoundCheckResult::isCertain);
            }
            Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = inheritanceResult.getPedigreeInheritanceMatches();
            pedigreeInheritanceMatches.add(new PedigreeInheritanceMatch(AR_C, !isCertain));
            inheritanceResult.setPedigreeInheritanceMatches(pedigreeInheritanceMatches);
            inheritanceResult.setCompounds(compoundsMap);
        }
    }

    private boolean isEmpty(Map<GeneInfo, Set<CompoundCheckResult>> compounds) {
        for(Set<CompoundCheckResult> compoundCheckResults : compounds.values()) {
            if(!compoundCheckResults.isEmpty()){
                return false;
            }
        }
        return true;
    }

    private void addToVcfRecordMap(VcfRecord vcfRecord, Map<GeneInfo, Set<VcfRecord>> vcfRecordGeneInfoMap) {
        for (GeneInfo geneInfo : vcfRecord.geneInfos()) {
            Set<VcfRecord> records = vcfRecordGeneInfoMap.containsKey(geneInfo) ? vcfRecordGeneInfoMap.get(geneInfo) : new HashSet<>();
            records.add(vcfRecord);
            vcfRecordGeneInfoMap.put(geneInfo, records);
        }
    }

    private Set<Pedigree> createFamilyFromVcf(VCFHeader fileHeader) {
        Set<Pedigree> familyList = new HashSet<>();
        ArrayList<String> sampleNames = fileHeader.getSampleNamesInOrder();
        for (String sampleName : sampleNames) {
            //no ped: unknown Sex, assume affected, no relatives, therefor the sampleId can be used as familyId
            Person person = Person.builder().familyId(sampleName).individualId(sampleName)
                    .paternalId("")
                    .maternalId("").sex(Sex.UNKNOWN)
                    .affectedStatus(AffectedStatus.AFFECTED).build();
            Sample sample = Sample.builder().person(person).proband(true).build();
            familyList.add(Pedigree.builder()
                    .id(sampleName).members(singletonMap(sampleName, sample)).build());
        }
        return familyList;
    }
}
