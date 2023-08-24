package org.molgenis.vcf.inheritance.matcher.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.inheritance.matcher.checker.PedigreeTestUtil;
import org.molgenis.vcf.utils.sample.model.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class InheritanceUtilsTest {
    @Test
    void testFilterBloodRelatives() {
        Sample sample = Sample.builder().person(Person.builder().individualId("Patient").sex(Sex.MALE).affectedStatus(AffectedStatus.AFFECTED).maternalId("Mother").paternalId("Father").familyId("TEST").build()).build();
        Pedigree actual = InheritanceUtils.filterBloodRelatives(PedigreeTestUtil.createExtendedFamily(), sample);

        Set<String> expectedMembers = Set.of("Patient","Mother","Father","Brother","Grandmother","Grandfather","GreatGrandfather","GreatGrandmother","Uncle");
        assertEquals(expectedMembers,actual.getMembers().keySet());
    }
}