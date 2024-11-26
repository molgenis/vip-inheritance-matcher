package org.molgenis.vcf.inheritance.matcher.vcf.meta;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.utils.metadata.*;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.model.metadata.NestedFieldMetadata;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.vcf.utils.metadata.ValueCount.Type.FIXED;

@ExtendWith(MockitoExtension.class)
class VepMetadataTest {
    @Mock
    VCFHeader vcfHeader;
    @Mock
    FieldMetadataService fieldMetadataService;

    @Test
    void getVepFieldId() {
        VCFInfoHeaderLine csqInfoHeaderLine = mock(VCFInfoHeaderLine.class);
        when(csqInfoHeaderLine.getID()).thenReturn("CSQ");
        when(csqInfoHeaderLine.getDescription()).thenReturn("Consequence annotations from Ensembl VEP. Format: Test|VIPC");
        when(vcfHeader.getInfoHeaderLines()).thenReturn(Set.of(csqInfoHeaderLine));
        NestedFieldMetadata nestedStrandMeta = NestedFieldMetadata.builder().index(0)
                .label("STRAND").description("STRAND")
                .type(ValueType.INTEGER).numberType(FIXED).numberCount(1).build();
        NestedFieldMetadata nestedTestMeta = NestedFieldMetadata.builder().index(1)
                .label("TEST label").description("TEST desc").type(ValueType.INTEGER)
                .numberType(ValueCount.Type.R).build();
        FieldMetadata csqMeta = FieldMetadata.builder().label("CSQ").description("Consequence annotations from Ensembl VEP. Format: STRAND|VIPC").numberType(ValueCount.Type.VARIABLE).type(ValueType.STRING).numberType(ValueCount.Type.VARIABLE).nestedFields(Map.of("STRAND", nestedStrandMeta, "VIPC", nestedTestMeta)).build();
        when(fieldMetadataService.load(vcfHeader, Map.of(FieldIdentifier.builder()
                .type(org.molgenis.vcf.utils.metadata.FieldType.INFO).name("CSQ").build(), NestedAttributes.builder()
                .prefix("Consequence annotations from Ensembl VEP. Format: ").seperator("|").build()))).thenReturn(FieldMetadatas.builder().format(Map.of()).info(Map.of("CSQ", csqMeta)).build());
        VepMetadata vepMetadata = new VepMetadata(vcfHeader, fieldMetadataService);

        assertEquals(1, vepMetadata.getClassIndex());
    }

    @Test
    void getVepFieldIdMissingVep() {
        VCFInfoHeaderLine csqInfoHeaderLine = mock(VCFInfoHeaderLine.class);
        when(csqInfoHeaderLine.getDescription()).thenReturn("NonVepDesc: Test|VIPC");
        when(vcfHeader.getInfoHeaderLines()).thenReturn(Set.of(csqInfoHeaderLine));

        assertThrows(MissingInfoException.class, () -> new VepMetadata(vcfHeader, fieldMetadataService));
    }
}