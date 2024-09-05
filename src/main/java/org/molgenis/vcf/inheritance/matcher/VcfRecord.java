package org.molgenis.vcf.inheritance.matcher;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import lombok.Getter;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.VcfRecordGenes;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class VcfRecord {
    private final VariantContext variantContext;
    @Getter
    private final VepMetadata vepMetadata;
    @Getter
    private final Set<String> pathogenicClasses;

    public VcfRecord(VariantContext variantContext, VepMetadata vepMetadata, Set<String> pathogenicClasses) {
        this.variantContext = requireNonNull(variantContext);
        this.vepMetadata = requireNonNull(vepMetadata);
        this.pathogenicClasses = requireNonNull(pathogenicClasses);
    }

    public EffectiveGenotype getGenotype(String sampleId){
        htsjdk.variant.variantcontext.Genotype gt = variantContext.getGenotype(sampleId);
        if(gt == null){
            return null;
        }
        return new EffectiveGenotype(gt, variantContext, getPathogenicAlleles());
    }

    private List<Allele> getPathogenicAlleles() {
        if(pathogenicClasses.isEmpty()){
            return variantContext.getAlternateAlleles();
        }
        return null;
    }

    public List<String> getAttributeAsStringList(String vepFieldId) {
        return variantContext.getAttributeAsStringList(vepFieldId, "");
    }

    public List<Allele> getAlternateAlleles() {
        return variantContext.getAlternateAlleles();
    }

    public VariantContext unwrap() {
        return variantContext;
    }

    public String getContig() {
        return variantContext.getContig();
    }

    public int getStart() {
        return variantContext.getStart();
    }

    public Allele getReference() {
        return variantContext.getReference();
    }


    public VcfRecordGenes getVcfRecordGenes() {
        return getVcfRecordGenes(emptyMap());
    }

    public VcfRecordGenes getVcfRecordGenes(Map<String, Gene> knownGenes) {
        VcfRecordGenes.VcfRecordGenesBuilder genesBuilder = VcfRecordGenes.builder();
        Map<String, Gene> genes = new HashMap<>();
        List<String> vepValues = getAttributeAsStringList(vepMetadata.getVepFieldId());
        for (String vepValue : vepValues) {
            String[] vepSplit = vepValue.split("\\|", -1);
            String gene = vepSplit[vepMetadata.getGeneIndex()];
            String source = vepSplit[vepMetadata.getGeneSourceIndex()];
            if (gene.isEmpty() || source.isEmpty()) {
                genesBuilder.containsVcWithoutGene(true);
                continue;
            }

            if (!knownGenes.containsKey(gene)) {
                Set<InheritanceMode> modes = new HashSet<>();
                if (vepMetadata.getInheritanceIndex() != -1) {
                    String[] inheritanceModes
                            = vepSplit[vepMetadata.getInheritanceIndex()].split("&");
                    mapGeneInheritance(modes, inheritanceModes);
                }
                genes.put(gene, new Gene(gene, source, modes));
            } else {
                genes.put(gene, knownGenes.get(gene));
            }
        }
        genesBuilder.genes(genes);
        return genesBuilder.build();
    }

    public Set<String> getClassesForAllele(int alleleIndex){
        List<String> vepValues = getAttributeAsStringList(vepMetadata.getVepFieldId());
        Set<String> classes = new HashSet<>();
        for (String vepValue : vepValues) {
            String[] vepSplit = vepValue.split("\\|", -1);
            int csqAlleleIndex = Integer.parseInt(vepSplit[vepMetadata.getAlleleNumIndex()]);
            if(csqAlleleIndex == alleleIndex){
                classes.add(vepSplit[vepMetadata.getClassIndex()]);
            }
        }
        return classes;
    }

    private void mapGeneInheritance(Set<InheritanceMode> modes, String[] inheritanceModes) {
        for (String mode : inheritanceModes) {
            switch (mode) {
                case "AR" -> modes.add(InheritanceMode.AR);
                case "AD" -> modes.add(InheritanceMode.AD);
                case "XLR" -> modes.add(InheritanceMode.XLR);
                case "XLD" -> modes.add(InheritanceMode.XLD);
                case "XL" -> {
                    modes.add(InheritanceMode.XLR);
                    modes.add(InheritanceMode.XLD);
                }
                default -> {
                    //We ignore all the modes that are not used for matching.
                }
            }
        }
    }
}
