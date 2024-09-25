package org.molgenis.vcf.inheritance.matcher.vcf;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.molgenis.vcf.inheritance.matcher.vcf.meta.VepMetadata;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.GeneInfo;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

public class VcfRecordFactoryImpl implements VcfRecordFactory {
    private final VepMetadata vepMetadata;

    public VcfRecordFactoryImpl(VepMetadata vepMetadata) {
        this.vepMetadata = requireNonNull(vepMetadata);
    }

    @Override
    public VcfRecord create(VariantContext variantContext, Set<String> pathogenicClasses) {
        Set<GeneInfo> geneInfos = getVcfGeneInfos(variantContext);
        Set<Allele> pathogenicAlleles = getPathogenicAlleles(variantContext, vepMetadata, pathogenicClasses);

        return new VcfRecord(variantContext, pathogenicAlleles, geneInfos);
    }

    private Set<GeneInfo> getVcfGeneInfos(VariantContext variantContext) {
        List<String> vepValues = getAttributeAsStringList(variantContext, vepMetadata.getVepFieldId());
        Set<GeneInfo> result = new HashSet<>();
        for (String vepValue : vepValues) {
            String[] vepSplit = vepValue.split("\\|", -1);
            String geneId = vepSplit[vepMetadata.getGeneIndex()];
            String symbolSource = vepSplit[vepMetadata.getGeneSourceIndex()];
            Set<InheritanceMode> inheritanceModes = vepMetadata.getInheritanceIndex() != -1 ? mapGeneInheritance(vepSplit[vepMetadata.getInheritanceIndex()]) : emptySet();
            result.add(new GeneInfo(geneId, symbolSource, inheritanceModes));
        }
        return result;
    }

    private Set<Allele> getPathogenicAlleles(VariantContext variantContext, VepMetadata vepMetadata, Set<String> pathogenicClasses) {
        Set<Allele> pathogenicAlleles = new HashSet<>();

        for (int i = 1; i <= variantContext.getAlternateAlleles().size(); i++) {
            Allele allele = variantContext.getAlleles().get(i);
            if (pathogenicClasses.isEmpty() || isAllelePathogenic(variantContext, vepMetadata, i, pathogenicClasses)) {
                pathogenicAlleles.add(allele);
            }
        }
        return pathogenicAlleles;
    }

    public boolean isAllelePathogenic(VariantContext variantContext, VepMetadata vepMetadata, int alleleIndex, Set<String> pathogenicClasses) {
        if (pathogenicClasses.isEmpty()) {
            return true;
        }
        if ((vepMetadata.getAlleleNumIndex() == -1 || vepMetadata.getClassIndex() == -1)) {
            throw new UnsupportedOperationException("Classes mapping provided, but input VCF is missing ALLELE_NUM or VIPC VEP annotations");
        }
        List<String> vepValues = getAttributeAsStringList(variantContext, vepMetadata.getVepFieldId());
        boolean result = false;
        for (String vepValue : vepValues) {
            String[] vepSplit = vepValue.split("\\|", -1);
            int csqAlleleIndex = Integer.parseInt(vepSplit[vepMetadata.getAlleleNumIndex()]);
            if (csqAlleleIndex == alleleIndex) {
                result = pathogenicClasses.contains(vepSplit[vepMetadata.getClassIndex()]);
            }
        }
        return result;
    }

    public List<String> getAttributeAsStringList(VariantContext variantContext, String vepFieldId) {
        return variantContext.getAttributeAsStringList(vepFieldId, "");
    }

    private Set<InheritanceMode> mapGeneInheritance(String inheritanceString) {
        Set<InheritanceMode> modes = new HashSet<>();
        String[] inheritanceModes = inheritanceString.split(",");
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
                case "YL" -> modes.add(InheritanceMode.YL);
                case "MT" -> modes.add(InheritanceMode.MT);
                default -> {
                    //We ignore all the modes that are not used for matching.
                }
            }
        }
        return modes;
    }
}
