package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AR;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XLD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.XLR;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum;

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
    when(vc.getAttributeAsStringList("CSQ", "")).thenReturn(Arrays.asList(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1||HGNC|17877||AD&AR|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123457|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XL&ICI|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123458|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XLR&XLD|Leber_congenital_amaurosis_9:AR|"));
    when(infoHeader.getID()).thenReturn("CSQ");
    when(infoHeader.getDescription()).thenReturn(
        "Consequence annotations from Ensembl VEP. Format: Allele|Consequence|IMPACT|SYMBOL|Gene|Feature_type|Feature|BIOTYPE|EXON|INTRON|HGVSc|HGVSp|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND|FLAGS|SYMBOL_SOURCE|HGNC_ID|HGVS_OFFSET|InheritanceModesGene|InheritanceModesPheno|PREFERRED");
    when(header.getInfoHeaderLines()).thenReturn(Collections.singletonList(infoHeader));
    when(vcfFileReader.getFileHeader()).thenReturn(header);
    vepMapper = new VepMapper(vcfFileReader);
  }

  @Test
  void getGeneInheritanceMap() {
    Map<String, Set<InheritanceModeEnum>> expected = new HashMap<>();
    expected.put("ENSG00000123456", Set.of(AR, AD));
    expected.put("ENSG00000123457", Set.of(XLD, XLR));
    expected.put("ENSG00000123458", Set.of(XLD, XLR));
    assertEquals(expected, vepMapper.getGeneInheritanceMap(vc));
  }

  @Test
  void getGenes() {
    Set<String> expected = Set.of("ENSG00000123456", "ENSG00000123457", "ENSG00000123458");
    assertEquals(expected, vepMapper.getGenes(vc));
  }
}