package org.molgenis.vcf.inheritance.matcher;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PedUtilsTest {

  @ParameterizedTest
  @ValueSource(strings = {"classpath:pedigree_commented.ped","classpath:pedigree_complex.ped","classpath:pedigree_spaces.ped"})
  void map(String path) throws FileNotFoundException {
    Path pedigree = ResourceUtils.getFile(path).toPath();

    Map<String, String> actual = PedUtils
        .map(Collections.singletonList(pedigree), Collections.singletonList("Patient"));

    Map<String, String> expected = Map.of("Patient","FAM001");
    assertEquals(expected, actual);
  }
}