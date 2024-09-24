package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.inheritance.matcher.checker.ArCompoundChecker;
import org.molgenis.vcf.inheritance.matcher.checker.DeNovoChecker;
import org.molgenis.vcf.inheritance.matcher.model.*;
import org.molgenis.vcf.utils.sample.model.*;

import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.inheritance.matcher.VariantContextUtils.getAltAlleles;
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

        Map<GeneInfo, Set<VariantGeneRecord>> vcfRecordGeneInfoMap = new HashMap<>();
        List<VariantRecord> variantRecords = vcfReader.stream().toList();

        for (VariantRecord variantRecord : variantRecords) {
            variantRecord.variantGeneRecords().values().forEach(variantGeneRecord -> {
                //Only perform matching if a pathogenic or vus allele is present
                if (!variantGeneRecord.getGeneInfo().geneId().isEmpty() && !variantGeneRecord.getPathogenicAlleles().isEmpty()) {
                    addToVcfRecordMap(variantGeneRecord, vcfRecordGeneInfoMap);
                }
            });
        }

        for (VariantRecord variantRecord : variantRecords) {
            Map<Pedigree, InheritanceResult> inheritanceResultMap = new HashMap<>();
            for (Pedigree pedigree : pedigrees) {
                Map<GeneInfo, InheritanceGeneResult> geneInheritanceResults = new HashMap<>();
                variantRecord.variantGeneRecords().values().forEach(variantGeneRecord -> {
                    InheritanceGeneResult geneInheritanceResult = InheritanceGeneResult.builder().geneInfo(variantGeneRecord.getGeneInfo()).build();
                    Set<Allele> altAllelesForPedigree = getAltAlleles(variantGeneRecord, pedigree);
                    //Only perform matching if a family member with a pathogenic allele is present
                    if (altAllelesForPedigree.stream().anyMatch(allele -> variantGeneRecord.getPathogenicAlleles().contains(allele))) {
                        addToVcfRecordMap(variantGeneRecord, vcfRecordGeneInfoMap);
                        Set<InheritanceMode> modes = Set.of(AD, AR, XLD, XLR, MT, YL);
                        modes.forEach(mode -> {
                            MatchEnum isMatch = pedigreeInheritanceChecker.check(variantGeneRecord, pedigree, mode);
                            if (isMatch != FALSE) {
                                geneInheritanceResult.addInheritanceMode(new PedigreeInheritanceMatch(mode, isMatch == POTENTIAL));
                            }
                        });
                        if (geneInheritanceResult.getPedigreeInheritanceMatches().stream().noneMatch(match -> match.inheritanceMode() == AD)) {
                            MatchEnum isAdIpMatch = pedigreeInheritanceChecker.check(variantGeneRecord, pedigree, AD_IP);
                            if (isAdIpMatch != FALSE) {
                                geneInheritanceResult.addInheritanceMode(new PedigreeInheritanceMatch(AD_IP, isAdIpMatch == POTENTIAL));
                            }
                        }
                    }
                    if (!variantGeneRecord.getGeneInfo().geneId().isEmpty()) {
                        Set<CompoundCheckResult> compounds = arCompoundChecker.check(vcfRecordGeneInfoMap, variantGeneRecord, pedigree);
                        if (!compounds.isEmpty()) {
                            geneInheritanceResult.setCompounds(compounds);
                            boolean isCertain = compounds.stream().anyMatch(CompoundCheckResult::isCertain);
                            Set<PedigreeInheritanceMatch> pedigreeInheritanceMatches = geneInheritanceResult.getPedigreeInheritanceMatches();
                            pedigreeInheritanceMatches.add(new PedigreeInheritanceMatch(AR_C, !isCertain));
                            geneInheritanceResult.setPedigreeInheritanceMatches(pedigreeInheritanceMatches);
                            geneInheritanceResult.setCompounds(compounds);
                            geneInheritanceResults.put(variantGeneRecord.getGeneInfo(), geneInheritanceResult);
                        }
                    }
                    geneInheritanceResults.put(variantGeneRecord.getGeneInfo(), geneInheritanceResult);
                });

                Map<Sample, MatchEnum> denovoResult = new HashMap<>();
                pedigree.getMembers().values().stream().filter(sample -> probands.isEmpty() || probands.contains(sample.getPerson().getIndividualId())).forEach(proband ->
                        denovoResult.put(proband, deNovoChecker.checkDeNovo(variantRecord, proband)));
                InheritanceResult inheritanceResult = InheritanceResult.builder().build();
                inheritanceResult.setInheritanceGeneResults(geneInheritanceResults.values());
                inheritanceResult.setDenovo(denovoResult);
                inheritanceResultMap.put(pedigree, inheritanceResult);
            }

            VariantContext annotatedVc = annotator.annotateInheritance(variantRecord, pedigrees, inheritanceResultMap, probands);
            recordWriter.add(annotatedVc);
        }
    }

    private void addToVcfRecordMap(VariantGeneRecord variantGeneRecord, Map<GeneInfo, Set<VariantGeneRecord>> vcfRecordGeneInfoMap) {
        GeneInfo geneInfo = variantGeneRecord.getGeneInfo();
        Set<VariantGeneRecord> records = vcfRecordGeneInfoMap.containsKey(geneInfo) ? vcfRecordGeneInfoMap.get(geneInfo) : new HashSet<>();
        records.add(variantGeneRecord);
        vcfRecordGeneInfoMap.put(geneInfo, records);
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
