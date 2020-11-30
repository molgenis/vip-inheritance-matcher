package org.molgenis.vcf.inheritance.matcher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.inheritance.matcher.Annotator.DENOVO;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MATCH;
import static org.molgenis.vcf.inheritance.matcher.Annotator.INHERITANCE_MODES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.MATCHING_GENES;
import static org.molgenis.vcf.inheritance.matcher.Annotator.POSSIBLE_COMPOUND;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AD;
import static org.molgenis.vcf.inheritance.matcher.model.InheritanceModeEnum.AR;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.inheritance.matcher.model.Annotation;
import org.molgenis.vcf.inheritance.matcher.model.InheritanceMode;
import org.molgenis.vcf.inheritance.matcher.model.SubInheritanceMode;

class AnnotatorTest {

  Annotator annotator = new Annotator();

  @Test
  void annotateHeader() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    annotator.annotateHeader(vcfHeader);
    verify(vcfHeader)
        .addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MODES, VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "Predicted inheritance modes."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(POSSIBLE_COMPOUND, 1,
        VCFHeaderLineType.Integer,
        "Possible compound status for AR inheritance modes, 1 = true, 0 = false."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(DENOVO, 1,
        VCFHeaderLineType.Integer,
        "Denovo status, 1 = true, 0 = false."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(INHERITANCE_MATCH, 1,
        VCFHeaderLineType.String,
        "Does inheritance match for sample and genes, 1 = true, 0 = false."));
    verify(vcfHeader).addMetaDataLine(new VCFFormatHeaderLine(MATCHING_GENES, VCFHeaderLineCount.UNBOUNDED,
        VCFHeaderLineType.String,
        "Genes for which inheritance modes of the sample and gene match."));
  }
}