package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenmodCompoundMapperTest {

  GenmodCompoundMapper mapper = new GenmodCompoundMapper();

  @Test
  void createVariantGeneList() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.getContig()).thenReturn("1");
    when(vc.getStart()).thenReturn(123);
    Allele ref = mock(Allele.class);
    when(ref.getBaseString()).thenReturn("A");
    when(vc.getReference()).thenReturn(ref);
    Allele alt = mock(Allele.class);
    when(alt.getBaseString()).thenReturn("T");
    when(vc.getAlternateAlleles()).thenReturn(Collections.singletonList(alt));
    VCFFileReader fileReader = mock(VCFFileReader.class);
    CloseableIterator<VariantContext> mockIterator = mock(CloseableIterator.class);
    when(fileReader.iterator()).thenReturn(mockIterator);
    when(mockIterator.hasNext()).thenReturn(true).thenReturn(false);
    when(mockIterator.next()).thenReturn(vc);
    VepMapper vepMapper = mock(VepMapper.class);
    when(vepMapper.getGenes(vc)).thenReturn(Set.of("GENE1", "GENE2"));

    Map<String, Set<String>> expected = new HashMap<>();
    expected.put("1_123_A_T", Set.of("GENE1", "GENE2"));

    assertEquals(expected, mapper.createVariantGeneList(fileReader, vepMapper));
  }

  @Test
  void mapCompounds() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.getAttributeAsStringList("Compounds", "")).thenReturn(
        Arrays.asList("FAM001:1_1234567_G_A", "T|1_1234568_C_T", "FAM002:1_1234568_C_T"));

    Map<String, String[]> actual = mapper.mapCompounds(vc);
    assertAll(
        () -> assertArrayEquals(new String[]{"1_1234567_G_A,T", "1_1234568_C_T"},
            actual.get("FAM001")),
        () -> assertArrayEquals(new String[]{"1_1234568_C_T"}, actual.get("FAM002"))
    );
  }
}