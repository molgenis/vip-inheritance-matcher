package org.molgenis.vcf.inheritance.matcher;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VcfReader implements AutoCloseable, Iterable<VariantContext> {

  private static final String INFO_DESCRIPTION_PREFIX =
      "Consequence annotations from Ensembl VEP. Format: ";
  public static final String GENE = "GENE";
  private final VCFFileReader vcfFileReader;
  private String vepField = null;
  private int geneIndex = -1;

  public VcfReader(VCFFileReader vcfFileReader) {
    this.vcfFileReader = requireNonNull(vcfFileReader);
    init();
  }

  private void init() {
    VCFHeader vcfHeader = vcfFileReader.getFileHeader();
    for (VCFInfoHeaderLine vcfInfoHeaderLine : vcfHeader.getInfoHeaderLines()) {
      if (canMap(vcfInfoHeaderLine)) {
        this.vepField = vcfInfoHeaderLine.getID();
        List<String> nestedInfo = getNestedInfoIds(vcfInfoHeaderLine);
        if (nestedInfo.contains(GENE)) {
          this.geneIndex = nestedInfo.indexOf(GENE);
        }else{
          throw new UnsupportedOperationException("Missing VEP(GENE) information.");
        }
      }
    else {
        throw new UnsupportedOperationException("Missing VEP annotation.");
      }
    }
  }

  @Override
  public Iterator<VariantContext> iterator() {
    return vcfFileReader.iterator();
  }

  @Override
  public void close() {
    vcfFileReader.close();
  }

  public VCFHeader getFileHeader(){
    return vcfFileReader.getFileHeader();
  }

  public Map<String, List<VariantContext>> getVariantsPerGene() {
    Map<String, List<VariantContext>> result = new HashMap<>();
    for (VariantContext vc : vcfFileReader) {
      Set<String> genes = getGenes(vc);
      for (String gene : genes) {
        List<VariantContext> variants;
        if (result.containsKey(gene)) {
          variants = result.get(gene);
        } else {
          variants = new ArrayList<>();
        }
        variants.add(vc);
        result.put(gene, variants);
      }
    }
    return result;
  }

  private static boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    // match on the description since the INFO ID is configurable (default: CSQ)
    String description = vcfInfoHeaderLine.getDescription();
    return description.startsWith(INFO_DESCRIPTION_PREFIX);
  }

  private Set<String> getGenes(VariantContext vc) {
    Set<String> genes = new HashSet<>();
    List<String> vepValues = vc.getAttributeAsStringList(vepField, "");
    for (String vepValue : vepValues) {
      String[] vepSplit = vepValue.split("\\|", -1);
      genes.add(vepSplit[geneIndex]);
    }
    return genes;
  }

  private static List<String> getNestedInfoIds(VCFInfoHeaderLine vcfInfoHeaderLine) {
    String description = vcfInfoHeaderLine.getDescription();
    String[] infoIds = description.substring(INFO_DESCRIPTION_PREFIX.length()).split("\\|", -1);
    return asList(infoIds);
  }
}
