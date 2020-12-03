package org.molgenis.vcf.inheritance.matcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.molgenis.vcf.inheritance.matcher.Annotator.DENOVO;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MATCH;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MODES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.MATCHING_GENES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.POSSIBLE_COMPOUND;

import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnotatorTest {

  private Annotator annotator;

  @BeforeEach
  void setUp() {
    annotator = new Annotator();
  }

  @Test
  void annotateHeader() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    annotator.annotateHeader(vcfHeader);
    verify(vcfHeader)
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "An enumeration of possible inheritance modes."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Compound status."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
        VCFHeaderLineType.Integer,
        "Inheritance Denovo status."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
        VCFHeaderLineType.String,
        "Inheritance Match status."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "Genes with an inheritance match."));
  }
}