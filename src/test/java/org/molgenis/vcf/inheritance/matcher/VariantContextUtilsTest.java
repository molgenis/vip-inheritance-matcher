package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariantContextUtilsTest {

  @Mock
  private VariantRecord variantContext;

  @Test
  void onAutosomeTrue() {
    when(variantContext.getContig()).thenReturn("1");
    assertTrue(VariantContextUtils.onAutosome(variantContext));
  }

  @Test
  void onAutosomeFalse() {
    assertFalse(VariantContextUtils.onAutosome(variantContext));
  }

  @Test
  void onChromosomeX() {
    when(variantContext.getContig()).thenReturn("X");
    assertTrue(VariantContextUtils.onChromosomeX(variantContext));
  }

  @Test
  void onChromosomeXFalse() {
    assertFalse(VariantContextUtils.onChromosomeX(variantContext));
  }

  @Test
  void onChromosomeY() {
    when(variantContext.getContig()).thenReturn("Y");
    assertTrue(VariantContextUtils.onChromosomeY(variantContext));
  }

  @Test
  void onChromosomeYFalse() {
    assertFalse(VariantContextUtils.onChromosomeY(variantContext));
  }

  @Test
  void onChromosomeMt() {
    when(variantContext.getContig()).thenReturn("MT");
    assertTrue(VariantContextUtils.onChromosomeMt(variantContext));
  }

  @Test
  void onChromosomeMtFalse() {
    assertFalse(VariantContextUtils.onChromosomeMt(variantContext));
  }
}