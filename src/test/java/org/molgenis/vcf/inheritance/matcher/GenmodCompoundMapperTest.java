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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenmodCompoundMapperTest {

  private GenmodCompoundMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new GenmodCompoundMapper();
  }

  @Test
  void createVariantGeneList() {
    VariantContext vc = mock(VariantContext.class);
    when(vc.getContig()).thenReturn("1");
    when(vc.getStart()).thenReturn(123);
    Allele ref = mock(Allele.class);
    when(ref.getDisplayString()).thenReturn("A");
    when(vc.getReference()).thenReturn(ref);
    Allele alt = mock(Allele.class);
    when(alt.getDisplayString()).thenReturn("T");
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

  private static Stream<Arguments> createGenmodVariantIdentifierProvider() {
    return Stream.of(
        Arguments.of("T", "TA", "X_123_T_TA"),
        Arguments.of("T", "<DEL>", "X_123_T_DEL"),
        Arguments.of("T", "<DUP:TANDEM>", "X_123_T_DUPTANDEM"),
        Arguments.of("T", "G]17:198982]", "X_123_T_G17198982"),
        Arguments.of("T", "C[2:321682[", "X_123_T_C2321682"),
        Arguments.of("<REF>", "T", "X_123_<REF>_T") // genmod only strips alt
    );
  }

  @ParameterizedTest
  @MethodSource("createGenmodVariantIdentifierProvider")
  void createGenmodVariantIdentifier(String refAllele, String altAllele, String variantId) {
    VariantContext variantContext = mock(VariantContext.class);
    when(variantContext.getContig()).thenReturn("X");
    when(variantContext.getStart()).thenReturn(123);
    when(variantContext.getReference()).thenReturn(Allele.create(refAllele));
    when(variantContext.getAlternateAlleles()).thenReturn(List.of(Allele.create(altAllele)));
    assertEquals(variantId, GenmodCompoundMapper.createGenmodVariantIdentifier(variantContext));
  }
}