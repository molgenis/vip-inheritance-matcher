package org.molgenis.vcf.inheritance.matcher.ped;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.ped.PedIndividual.AffectionStatus;
import org.molgenis.vcf.inheritance.matcher.ped.PedIndividual.Sex;

@ExtendWith(MockitoExtension.class)
class PedReaderTest {

  @Test
  void iterator() throws IOException {
    String ped = "";
    ped += "# my comment\n";
    ped += "FAM001  1  2 3  0  2\n";
    ped += "FAM001  2  0 0  1  1\n";
    ped += "# my individual comment\n";
    ped += "FAM001  3  0 0  2  0\n";

    try (PedReader pedReader = new PedReader(new StringReader(ped))) {
      List<PedIndividual> pedIndividuals = new ArrayList<>();
      pedReader.iterator().forEachRemaining(pedIndividuals::add);

      String familyId = "FAM001";
      List<PedIndividual> expectedPedIndividuals =
          List.of(
              new PedIndividual(familyId, "1", "2", "3", Sex.UNKNOWN, AffectionStatus.AFFECTED),
              new PedIndividual(familyId, "2", "0", "0", Sex.MALE, AffectionStatus.UNAFFECTED),
              new PedIndividual(familyId, "3", "0", "0", Sex.FEMALE, AffectionStatus.UNKNOWN));
      assertEquals(expectedPedIndividuals, pedIndividuals);
    }
  }

  @Test
  void iteratorUnsupportedPhenotype() throws IOException {
    String ped = "FAM001  1  2 3  0  HP:0011675\n";

    try (PedReader pedReader = new PedReader(new StringReader(ped))) {
      Iterator<PedIndividual> iterator = pedReader.iterator();
      assertThrows(UnsupportedPedException.class, iterator::next);
    }
  }

  @Test
  void iteratorUnsupportedGenotypes() throws IOException {
    String ped = "FAM001  1  0 0  1  2  A A  G G  A C\n";

    try (PedReader pedReader = new PedReader(new StringReader(ped))) {
      Iterator<PedIndividual> iterator = pedReader.iterator();
      assertThrows(InvalidPedException.class, iterator::next);
    }
  }

  @Test
  void iteratorNoSuchElementException() throws IOException {
    String ped = "FAM001  1  2 3  0  2\n";

    try (PedReader pedReader = new PedReader(new StringReader(ped))) {
      Iterator<PedIndividual> iterator = pedReader.iterator();
      iterator.next();
      assertThrows(NoSuchElementException.class, iterator::next);
    }
  }

  @Test
  void iteratorInvalidPedException() throws IOException {
    String ped = "invalid ped data";

    try (PedReader pedReader = new PedReader(new StringReader(ped))) {
      Iterator<PedIndividual> iterator = pedReader.iterator();
      assertThrows(InvalidPedException.class, iterator::next);
    }
  }
}
