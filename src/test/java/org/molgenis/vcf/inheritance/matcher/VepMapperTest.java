package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.AD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.XLD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceMode.XLR;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Gene;

@ExtendWith(MockitoExtension.class)
class VepMapperTest {

  @Mock
  VariantContext vc;
  private VepMapper vepMapper;

  @BeforeEach
  void setUp() {
    VCFFileReader vcfFileReader = mock(VCFFileReader.class);
    VCFHeader header = mock(VCFHeader.class);
    VCFInfoHeaderLine infoHeader = mock(VCFInfoHeaderLine.class);
    when(infoHeader.getID()).thenReturn("CSQ");
    when(infoHeader.getDescription()).thenReturn(
        "Consequence annotations from Ensembl VEP. Format: Allele|Consequence|IMPACT|SYMBOL|Gene|Feature_type|Feature|BIOTYPE|EXON|INTRON|HGVSc|HGVSp|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND|FLAGS|SYMBOL_SOURCE|HGNC_ID|HGVS_OFFSET|InheritanceModesGene|InheritanceModesPheno|PREFERRED");
    when(header.getInfoHeaderLines()).thenReturn(Collections.singletonList(infoHeader));
    when(vcfFileReader.getFileHeader()).thenReturn(header);
    vepMapper = new VepMapper(vcfFileReader);
  }

  @Test
  void getGenes() {
    when(vc.getAttributeAsStringList("CSQ", "")).thenReturn(Arrays.asList(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1||HGNC|17877||AD&AR|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123457|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XL&ICI|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123458|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XLR&XLD|Leber_congenital_amaurosis_9:AR|"));

    Map<String, Gene> expected = Map
        .of("ENSG00000123457", new Gene("ENSG00000123457","HGNC", false,  Set.of(XLD, XLR)), "ENSG00000123456",
            new Gene("ENSG00000123456","HGNC", false,  Set.of(AD, AR)), "ENSG00000123458",
            new Gene("ENSG00000123458","HGNC", false,  Set.of(XLD, XLR)));
    assertEquals(expected, vepMapper.getGenes(vc));
  }

  @Test
  void getGenesEmptyValues() {
    VariantContext vc2 = mock(VariantContext.class, "vc2");
    when(vc2.getAttributeAsStringList("CSQ", "")).thenReturn(Arrays.asList(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1||HGNC|17877||AD&AR|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE||ENSG00000123457|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XL&ICI|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|||Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XLR&XLD|Leber_congenital_amaurosis_9:AR|"));

    Map<String, Gene> expected = Map
        .of("ENSG00000123457", new Gene("ENSG00000123457","HGNC", false,  Set.of(XLD, XLR)), "ENSG00000123456",
            new Gene("ENSG00000123456","HGNC", false,  Set.of(AD, AR)));
    assertEquals(expected, vepMapper.getGenes(vc2));
  }

  @Test
  void getGenesEmptySymbolSource() {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getAttributeAsStringList(eq("CSQ"), any())).thenReturn(List.of(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1|||17877||AD&AR|Leber_congenital_amaurosis_9:AR|"));

    assertEquals(Map.of(), vepMapper.getGenes(variantContext));
  }
}