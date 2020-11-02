package org.molgenis.vcf.inheritance.matcher.ped;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pedigree parser (see http://zzz.bwh.harvard.edu/plink/data.shtml#ped) with fixed coding scheme
 * for affection scheme and sex <br>
 *
 * <p>Affection status:
 *
 * <ul>
 *   <li>-9 missing
 *   <li>0 missing
 *   <li>1 unaffected
 *   <li>2 affected
 * </ul>
 *
 * <p>Sex:
 *
 * <ul>
 *   <li>1 male
 *   <li>2 female
 *   <li>other unknown
 * </ul>
 *
 * <p>Unsupported features:
 *
 * <ul>
 *   <li>Phenotype values other than affection status
 *   <li>Genotypes
 * </ul>
 */
public class PedReader implements AutoCloseable {
  private final BufferedReader bufferedReader;
  private final PedIndividualParser pedIndividualParser;

  public PedReader(Reader reader) {
    this.bufferedReader =
        reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    this.pedIndividualParser = new PedIndividualParser();
  }

  public Iterator<PedIndividual> iterator() {
    return new Iterator<>() {
      PedIndividual nextPedIndividual = null;

      @Override
      public boolean hasNext() {
        if (nextPedIndividual == null) {
          try {
            readPedIndividual();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }
        return nextPedIndividual != null;
      }

      @Override
      public PedIndividual next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        PedIndividual pedIndividual = nextPedIndividual;
        nextPedIndividual = null;
        return pedIndividual;
      }

      private void readPedIndividual() throws IOException {
        String line;
        do {
          line = bufferedReader.readLine();
        } while (line != null && line.startsWith("#"));

        if (line != null) {
          nextPedIndividual = pedIndividualParser.parse(line);
        }
      }
    };
  }

  @Override
  public void close() throws IOException {
    bufferedReader.close();
  }
}
