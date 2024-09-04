package org.molgenis.vcf.inheritance.matcher;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
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
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.model.Gene;
import org.molgenis.vcf.inheritance.matcher.model.VariantContextGenes;
import org.molgenis.vcf.utils.metadata.FieldMetadataService;
import org.molgenis.vcf.utils.model.FieldMetadata;
import org.molgenis.vcf.utils.model.NestedField;
import org.molgenis.vcf.utils.model.NumberType;

@ExtendWith(MockitoExtension.class)
class VepMapperTest {

  @Mock
  VariantContext vc;
  @Mock
  FieldMetadataService fieldMetadataService;

  private VepMapper vepMapper;

  @BeforeEach
  void setUp() {
    VcfReader vcfReader = mock(VcfReader.class);
    VCFHeader header = mock(VCFHeader.class);
    VCFInfoHeaderLine infoHeader = mock(VCFInfoHeaderLine.class);
    when(infoHeader.getID()).thenReturn("CSQ");
    when(infoHeader.getDescription()).thenReturn(
        "Consequence annotations from Ensembl VEP. Format: Allele|Consequence|IMPACT|SYMBOL|Gene|Feature_type|Feature|BIOTYPE|EXON|INTRON|HGVSc|HGVSp|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND|FLAGS|SYMBOL_SOURCE|HGNC_ID|HGVS_OFFSET|InheritanceModesGene|InheritanceModesPheno|PREFERRED");
    HashMap<String, NestedField> vepMeta = new HashMap<>();

    vepMeta.put("Gene", org.molgenis.vcf.utils.model.NestedField.builder().index(4).numberCount(1)
        .numberType(NumberType.NUMBER)
        .type(org.molgenis.vcf.utils.model.ValueType.STRING).label("Gene").description("Gene")
        .build());
    vepMeta.put("SYMBOL_SOURCE", org.molgenis.vcf.utils.model.NestedField.builder().index(21).numberCount(1)
        .numberType(NumberType.NUMBER)
        .type(org.molgenis.vcf.utils.model.ValueType.STRING).label("Gene").description("Gene")
        .build());
    vepMeta.put("InheritanceModesGene", org.molgenis.vcf.utils.model.NestedField.builder().index(24).numberCount(1)
        .numberType(NumberType.NUMBER)
        .type(org.molgenis.vcf.utils.model.ValueType.STRING).label("Gene").description("Gene")
        .build());
    vepMeta.put("IncompletePenetrance", org.molgenis.vcf.utils.model.NestedField.builder().index(-1).numberCount(1)
        .numberType(NumberType.NUMBER)
        .type(org.molgenis.vcf.utils.model.ValueType.STRING).label("Gene").description("Gene")
        .build());
    when(fieldMetadataService.load(infoHeader)).thenReturn(
        FieldMetadata.builder().nestedFields(vepMeta).build());
    when(header.getInfoHeaderLines()).thenReturn(Collections.singletonList(infoHeader));
    vepMapper = new VepMapper(header, fieldMetadataService);
  }

  @Test
  void getGenes() {
    when(vc.getAttributeAsStringList("CSQ", "")).thenReturn(Arrays.asList(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1||HGNC|17877||AD&AR|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123457|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XL&ICI|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|TEST2|ENSG00000123458|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XLR&XLD|Leber_congenital_amaurosis_9:AR|"));

    VariantContextGenes expected = VariantContextGenes.builder()
            .genes(Map.of("ENSG00000123457", new Gene("ENSG00000123457","HGNC",  Set.of(XLD, XLR)), "ENSG00000123456",
            new Gene("ENSG00000123456","HGNC",  Set.of(AD, AR)), "ENSG00000123458",
            new Gene("ENSG00000123458","HGNC",  Set.of(XLD, XLR)))).build();
    assertEquals(expected, vepMapper.getGenes(new VcfRecord(vc, emptyList())));
  }

  @Test
  void getGenesEmptyValues() {
    VariantContext vc2 = mock(VariantContext.class, "vc2");
    when(vc2.getAttributeAsStringList("CSQ", "")).thenReturn(Arrays.asList(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1||HGNC|17877||AD&AR|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE||ENSG00000123457|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XL&ICI|Leber_congenital_amaurosis_9:AR|",
        "T|missense_variant|MODERATE|||Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>T|ENSP00000366410.1:p.Arg207Trp|763|619|207|R/W|Cgg/Tgg|||1||HGNC|17877||XLR&XLD|Leber_congenital_amaurosis_9:AR|"));

    VariantContextGenes expected = VariantContextGenes.builder().containsVcWithoutGene(true).genes(Map
            .of("ENSG00000123457", new Gene("ENSG00000123457", "HGNC", Set.of(XLD, XLR)), "ENSG00000123456",
                    new Gene("ENSG00000123456", "HGNC", Set.of(AD, AR)))).build();
    assertEquals(expected, vepMapper.getGenes(new VcfRecord(vc2, emptyList())));
  }

  @Test
  void getGenesEmptySymbolSource() {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getAttributeAsStringList(eq("CSQ"), any())).thenReturn(List.of(
        "G|missense_variant|MODERATE|TEST1|ENSG00000123456|Transcript|ENST00000377205|protein_coding|5/5||ENST00000377205.1:c.619C>G|ENSP00000366410.1:p.Arg207Gly|763|619|207|R/G|Cgg/Ggg|||1|||17877||AD&AR|Leber_congenital_amaurosis_9:AR|"));

    assertEquals(VariantContextGenes.builder().genes(Map.of()).containsVcWithoutGene(true).build(), vepMapper.getGenes(new VcfRecord(variantContext, emptyList())));
  }
}