package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContigUtilsTest {

  @ParameterizedTest
  @ValueSource(strings = {"1", "chr1", "chr1_xxxxx", "11", "chr11", "chr11_xxxxx"})
  void isAutosomeTrue(String contigId) {
    assertTrue(ContigUtils.isAutosome(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"X", "chrX", "chrX_xxxxx", "Y", "chrY", "chrY_xxxxx", "MT", "chrM"})
  void isAutosomeFalse(String contigId) {
    assertFalse(ContigUtils.isAutosome(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"X", "chrX", "chrX_xxxxx"})
  void isChromosomeXTrue(String contigId) {
    assertTrue(ContigUtils.isChromosomeX(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "chr1", "chr1_xxxxx", "Y", "chrY", "chrY_xxxxx", "MT", "chrM"})
  void isChromosomeXFalse(String contigId) {
    assertFalse(ContigUtils.isChromosomeX(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Y", "chrY", "chrY_xxxxx"})
  void isChromosomeYTrue(String contigId) {
    assertTrue(ContigUtils.isChromosomeY(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "chr1", "chr1_xxxxx", "X", "chrX", "chrX_xxxxx", "MT", "chrM"})
  void isChromosomeYFalse(String contigId) {
    assertFalse(ContigUtils.isChromosomeY(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"MT", "chrM"})
  void isChromosomeMtTrue(String contigId) {
    assertTrue(ContigUtils.isChromosomeMt(contigId));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "chr1", "chr1_xxxxx", "X", "chrX", "chrX_xxxxx", "Y", "chrY", "chrY_xxxxx"})
  void isChromosomeMtFalse(String contigId) {
    assertFalse(ContigUtils.isChromosomeMt(contigId));
  }
}