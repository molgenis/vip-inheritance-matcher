[![Build Status](https://app.travis-ci.com/molgenis/vip-inheritance-matcher.svg?branch=main)](https://app.travis-ci.com/molgenis/vip-inheritance-matcher)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-inheritance-matcher&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-inheritance-matcher)
# Variant Interpretation Pipeline - Inheritance Matcher
annotates VCF samples with denovo and possible compound flags and matching inheritance modes and genes.

## Requirements
- Java 21

Input VCF file should contain single ALT alleles per line and be annotated VEP.
Input should be annotated with [VIP inheritance VEP plugin](https://github.com/molgenis/vip/blob/master/resources/vep/plugins/Inheritance.pm) For full functionality.


## Installation
Generate a personal access token in GitHub with at least the scope "read:packages".

Then add a settings.xml to your Maven .m2 folder, or edit it if you already have one. It should
contain the following:
```
<?xml version="1.0"?>

<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/SETTINGS/1.0.0">
  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          </repository>
          <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/molgenis/vip-utils</url>
            <snapshots>
              <enabled>true</enabled>
            </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>[YOUR VIP USERNAME]</username>
      <password>[YOUR PERSONAL ACCESS TOKEN]</password>
    </server>
   </servers>
</settings>
```

## Added Sample information
```
##FORMAT=<ID=VI,Number=.,Type=String,Description="An enumeration of possible inheritance modes.">
##FORMAT=<ID=VIC,Number=1,Type=String,Description="List of possible compound hetrozygote variants.">
##FORMAT=<ID=VID,Number=1,Type=Integer,Description="Inheritance Denovo status.">
##FORMAT=<ID=VIG,Number=.,Type=String,Description="Genes with an inheritance match.">
##FORMAT=<ID=VIM,Number=1,Type=Integer,Description="Inheritance Match status.">
##FORMAT=<ID=VIS,Number=.,Type=String,Description="An enumeration of possible sub inheritance modes like e.g. compound, non penetrance.">
```

## Usage
```
usage: java -jar vcf-inheritance-matcher.jar -i <arg> -m <arg> [-o <arg>] [-pd
       <arg>] [-pb <arg>] [-np <arg>] [-c] [-f] [-d]
 -i,--input <arg>            Input VCF file (.vcf or .vcf.gz).
 -m,--metadata <arg>         VCF metadata file (.json).
 -o,--output <arg>           Output VCF file (.vcf or .vcf.gz).
 -pd,--pedigree <arg>        Comma-separated list of pedigree files
                             (.ped).
 -pb,--probands <arg>        Comma-separated list of proband individual
                             identifiers.
 -c,--classes <arg>   	     Comma-separated list of values in the INFO/CSQ VIPC subfield 
                             to be used in inheritance calculation. 
                             By default inheritance is calculated for all records.
 -f,--force                  Override the output file if it already
                             exists.
 -d,--debug                  Enable debug mode (additional logging).
```

## Inheritance patterns
- AR: Autosomal recessive
- AD: Autosomal dominant
- XLR: X-linked recessive
- XLD: X-linked dominant
- YL: Y-linked
- MT: Mitochondrial
- AR_C: Autosomal recessive compound hetrozygote
- AD_IP: Autosomal dominant incomplete penetrance

## Inheritance pattern rules
### General rules
For inheritance matching all the members in a family are considered.
This also means that all members in one family are assumed to be blood relatives to the proband(s).
If a pedigree contains one or more members with an unknown affected status, then:
- Inheritance match becomes potential if it would be a match based on members with a known affected status
- The match stays false if it is false based on members with a known affected status
  For all patterns applies that a homozygote reference call for an affected family member means the pattern does not match.
The list of supported contigs to determine if a variant is on X,Y,MT or an autosome can be found [here](https://github.com/molgenis/vip-inheritance-matcher/blob/main/src/main/java/org/molgenis/vcf/inheritance/matcher/ContigUtils.java)

#### Autosomal Dominant
1) The variant is not on chromosome X,Y or MT.
2) Affected members need to have at least one alternative allele.
3) Unaffected members cannot have an alternative allele that was also the single alternative allele for any affected member
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the pattern match will stay false.
5) If based on other members the pattern does match:
	- If affected members have one missing allele and one alternative allele, the inheritance match will still be true.
	- If affected members have one missing allele and one reference allele, or both alleles are missing values, the inheritance match will be "potential".
	- If unaffected members have one missing allele and one alternative allele, the inheritance match will be false if so based on rule 3, and potential if rule 3 would lead to a match.
	- If unaffected members have one missing allele and one reference allele, or both alleles are missing values, the inheritance match will be "potential".
##### Examples
| Patient | Father | Mother | Result    | Explanation                                                                                                                                                                |
|---------|--------|--------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0/1     | 0/0    | 0/0    | true      | De novo alternative allele for the patient, both parents have homozygotic reference genotypes.                                                                             |
| 0/1     | 0/1    | 0/0    | false     | Unaffected father has the same genotype.                                                                                                                                   |
| 0/1     | 0/2    | 0/0    | potential | Father has a different alternative allele, this causes a potential match since we do not know if this has the same pathogenicity as the alternative allele of the patient. |
| 0/1     | 0/2    | 0/1    | false     | Unaffected mother has the same genotype.                                                                                                                                   |
| 0/.     | 0/0    | 0/1    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                |
| 0/.     | 0/2    | 0/1    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                |
| 0/1     | 0/.    | 0/0    | potential | Father missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the match is "potential".                            |

#### Autosomal Dominant incomplete penetrance
1) The variant is not on chromosome X,Y or MT.
2) Affected members need to have at least one alternative allele.
3) Unaffected members can have any genotype
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the pattern match will stay false.
5) If based on other members the pattern does match:
	- If affected members have one missing allele and one alternative allele, the inheritance match will still be true.
	- If affected members have one missing allele and one reference allele, or both alleles are missing values, the inheritance match will be "potential".
##### Examples
| Patient | Father | Mother | Result | Explanation                                                                                                        |
|---------|--------|--------|--------|--------------------------------------------------------------------------------------------------------------------|
| 0/1     | 0/0    | 0/0    | true   | De novo alternative allele for the patient, both parents have homozygotic reference genotypes.                     |
| 0/1     | 0/1    | 0/0    | true   | Due to incomplete penetrance the unaffected parent can also have pathogenic alternative alleles in their genotype. |
| 0/1     | 0/2    | 0/0    | true   | Due to incomplete penetrance the unaffected parent can also have pathogenic alternative alleles in their genotype. |
| 0/1     | 0/2    | 0/1    | true   | Due to incomplete penetrance the unaffected parent can also have pathogenic alternative alleles in their genotype. |

#### Autosomal Recessive
1) The variant is not on chromosome X,Y or MT.
2) Affected members need to have at least two alternative alleles.
3) Unaffected members cannot have a genotype of which both alleles are present in a affected member.
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the pattern match will stay false.
5) If based on other members the pattern does match:
	- If affected members have one missing allele and one alternative allele, or both alleles are missing values, the inheritance match will be potential.
	- If affected members have one missing allele and one reference allele, the inheritance match will be false.
	- If unaffected members have one missing allele and one alternative allele, or both alleles are missing values, the inheritance match will be potential.
	- If unaffected members have one missing allele and one reference allele, the inheritance match will be true.
##### Examples
| Patient | Father | Mother | Result    | Explanation                                                                                                                                                                                                                                                                                       |
|---------|--------|--------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0/1     | 0/0    | 0/0    | false     | Patient genotype is not homozygote alternative.                                                                                                                                                                                                                                                   |
| 1/1     | 0/1    | 1/0    | true      | Patient genotype is homozygote altenative, parents are both hetrozygotes.                                                                                                                                                                                                                         |
| 1/2     | 2/2    | 1/1    | false     | Both father and mother genotypes are homozygote alternative, although the genotype does not match that of the patient there is still no scenario in which AR would be a suitable pattern, if either one of the alternative alleles is benign the patient ends up with only one pathogenic allele. |
| 2/2     | 1/1    | 1/0    | potential | Father genotype also is homozygote alternative, but for another allele than the patient, this leads to a potential match since we do not know if those alleles match in pathogenicity.                                                                                                            |
| 1/.     | 0/0    | 0/1    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                                                                                                                                       |
| 1/.     | 1/2    | 0/1    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                                                                                                                                       |
| 1/.     | 1/1    | 0/1    | potential | Patient missing allele can be anything, but since father is homozygotic for the known alternative allele of the patient, there can be no case that the autosomal recessive pattern matches.                                                                                                       |
| 1/1     | 1/.    | 0/0    | potential | Father missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the match is "potential".                                                                                                                                                   |

#### Compound Autosomal Recessive
1) Two variant are present in the same gene for all affected members.
2) Both those variants are not matching the AR inheritance pattern.
3) The variants are not on chromosome X,Y or MT.
4) Affected members need to have at least one variant in for both variants, if data is phased and both variants are in the same block, the variants have to be on different alleles.
5) Unaffected members can have one or both the same variants as an affected member for both variants if data is unphased, if the member has both variants the match is "potential" since the match depends on the variants being on the same allele or not.
6) if data is phased and both variants are in the same block, the unaffected sample can have both variants if they are on the same allele.
##### - Missing/partial genotypes:
6) If based on other members the pattern does not match the pattern match will stay false.
7) If based on other members the pattern does match:
	- If affected members have one missing allele or both alleles missing for one or both of the variants the pattern is a potential match.
	- If unaffected members have missing alleles in combination with an alternative allele, that has also been seen as a single alternative allele in genotypes of affected members, for both variants that this pattern does not match.
	- Other combinations of genotypes with missing alleles will lead to a "potential" match.
##### Examples
| Patient*   | Father*    | Mother*    | Result    | Explanation                                                                                                                                                                                                      |
|------------|------------|------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0/1  0/1   | 0/1  0/0   | 0/0  0/1   | true      | Patient is the only one with 2 variants in its genotypes.                                                                                                                                                        |
| 0/1  0/1   | 0/1  0/1   | 0/0  0/1   | potential | Father also has 2 variants in his genotypes, but data is unphased therefor it is unclear if those are on the same allele.                                                                                        |
| 0\|1  1\|0 | 0\|1  0\|1 | 0\|0  0\|1 | true      | Father also has 2 variants in his genotypes, but they both affect the same allele.                                                                                                                               |
| 0\|1  1\|0 | 0\|1  1\|0 | 0\|0  0\|1 | false     | Father also has 2 variants in his genotypes, and they affect both alleles.                                                                                                                                       |
| 0\|1  0\|1 | 0\|1  0\|0 | 0\|0  0\|1 | false     | The variants for the patient are on the same allele and therefor no compound.                                                                                                                                    |
| 0/1  0/1   | 0/1  0/2   | 0/0  0/1   | potential | Father has 2 genotypes with a variant, but one of those differs from the patients alternative allele, we do not know if those match in pathogenicity.                                                            |
| 0/1  0/1   | 1/1  0/0   | 0/0  0/1   | false     | Father has 2 alternative alleles for one of the variants but since he is unaffected it cannot be pathogenic, therefor the hetrozygotic variant in the patient genotype can not be part of a pathogenic compound. |
| 0/1  0/1   | 2/2  0/0   | 0/0  0/1   | potential | Father has 2 alternative alleles for one of the variants but of a different alternative allele than the alternative allele of the patient, we do not know if those alleles match in pathogenicity..              |
| 0/1  0/1   | 0/1  0/.   | 0/0  0/1   | potential | Since the father missing alleles can be anything; the alternative allele of mother genotype, the reference allele, or another alt allele, therefor the match is "potential".                                     |
| 0/1  0/.   | 1/1  0/0   | 0/0  0/1   | potential | Since the patient missing allele can be anything this is a possible match.                                                                                                                                       |
| 0/.  0/.   | 0/1  0/0   | 0/0  0/1   | potential | Since the patient missing alleles can be anything this is a possible match.                                                                                                                                      |
| 0/1  0/1   | 0/.  0/0   | 0/0  0/1   | true      | Father can have one of the variants of the patient, therefor the missing allele can be anything while the autosomal recessive pattern still matches.                                                             |
\*: every individual has 2 genotypes for 2 different variants in the same gene

#### X-linked Dominant
1) The variant is  on chromosome X.
2) Affected members need to have at least one alternative allele.
3) Unaffected members can only have an alternative allele that was also the single alternative allele for any affected member if the genotype is diploid (female), this is possible due to x inactivation.
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the pattern match will stay false.
5) If based on other members the pattern does match:
	- If affected members have one missing allele and one alternative allele, the pattern match will still be true.
	- If affected members have one missing allele and one reference allele, or the genotype (either haploid or diploid) is missing, the inheritance match will be "potential".
	- If unaffected members have one missing allele or the genotype (either haploid or diploid) is missing, the inheritance match will be "potential".
##### Examples
| Patient | Father | Mother | Result    | Explanation                                                                                                                                                                         |
|---------|--------|--------|-----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0/1     | 0      | 0/0    | true      | Patient is the only one with an alternative allele.                                                                                                                                 |
| 0/1     | 0      | 0/1    | true      | Unaffected mother can have one alternative allele due to X-inactivation.                                                                                                            |
| 0/1     | 1      | 0/0    | false     | Unaffected father has the same alternative allele.                                                                                                                                  |
| 0/1     | 0      | 1/1    | false     | Unaffected individuals cannot have a genotype that is entirely made up of the alternative allele.                                                                                   |
| 0/1     | 2      | 0/0    | potential | Father genotype also has an alternative allele, but for another one than the patient, this leads to a potential match since we do not know if those alleles match in pathogenicity. |
| 0/1     | 0      | 0/.    | true      | Unaffected mother can have one alternative allele due to X-inactivation, therefor the missing allele in the mother genotype has no effect.                                          |
| 0/.     | 0      | 0/0    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                         |
| 0/.     | 0      | 0/2    | potential | Patient missing allele can be anything; the alternative allele of mother genotype, the reference allele, or another alt allele, therefor the match is "potential".                  |
| 0/1     | .      | 0/0    | potential | Father missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the match is "potential".                                     |

#### X-linked Recessive
1) The variant is on chromosome X.
2) Affected members cannot have a reference allele.
3) Unaffected members cannot have a genotype of which all alleles are present in a affected member.
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the pattern match will stay false.
5) If based on other members the pattern does match:
	- If affected members have one missing allele and one alternative allele, or the entire genotype is missing, the inheritance match will be potential.
	- If affected members have one missing allele and one reference allele, the pattern match will be false.
	- If unaffected members have one missing allele and one alternative allele, or the genotype (either haploid or diploid) is missing, the inheritance match will be potential.
	- If unaffected members have one missing allele and one reference allele, the pattern match will be true.
##### Examples
| Patient | Father | Mother | Result    | Explanation                                                                                                                                                                            |
|---------|--------|--------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1/1     | 0      | 0/1    | true      | Patient is the only one with a genotype with only alternative alleles.                                                                                                                 |
| 1/2     | 0      | 0/1    | false     | Patient is the only one with a genotype with only alternative alleles.                                                                                                                 |
| 0/1     | 0      | 0/0    | false     | Patient genotype is not homozygote alternative.                                                                                                                                        |
| 1/1     | 1      | 1/0    | false     | Father has a genotypes that is entirely made up of the same alternative alleles as the patient.                                                                                        |
| 1/2     | 2      | 1/1    | false     | Both father and mother genotypes are entirely made up of the same alternative alleles as the patient.                                                                                  |
| 2/2     | 2      | 0/0    | potential | Father genotype also is homozygote alternative, but for another allele than the patient, this leads to a potential match since we do not know if those alleles match in pathogenicity. |
| 1/.     | 0      | 0/1    | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                                                            |
| 1/.     | 1      | 0/1    | false     | Patient missing allele can be anything, however the known alternative allele of the patient cannot be pathogenic since the unaffected father genotype contains it.                     |
| 1/.     | 2      | 0/1    | potential | Patient missing allele can be anything; the missing allele from alternative allele of father genotype, the reference allele, or another alt allele, therefor the match is "potential". |
| 1/1     | .      | 0/0    | potential | Father missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the match is "potential".                                        |

#### Y-linked
1) The variant is  on chromosome Y.
2) Only genotypes of male family members are taken into account.
3) Affected members need to have an alternative allele.
4) Unaffected members cannot have an alternative allele that was also the alternative allele for any affected member.
##### - Missing/partial genotypes:
5) If based on other members the pattern does not match the result will stay false.
6) If based on other members the pattern does match:
	- If any members have a missing genotype the pattern match will be 'potential'.
##### Examples
| Patient | Father | Result    | Explanation                                                                                                                                      |
|---------|--------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| 1       | 0      | true      | Patient is the only one with an alternative allele.                                                                                              |
| 1       | 1      | false     | Unaffected father has the same genotype as the patient.                                                                                          |
| 1       | 2      | potential | Unaffected father also has an alternative allele, but a different one than the patient, we do not know if those alleles match in pathogenicity.  |
| .       | 1      | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                      |
| 1       | .      | potential | Father missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the result is "potential". |

#### Mitochondrial
1) The variant is  on chromosome Y.
2) Affected members need to have an alternative allele.
3) Unaffected members cannot have an alternative allele that was also the alternative allele for any affected member.
##### - Missing/partial genotypes:
4) If based on other members the pattern does not match the result will stay false.
5) If based on other members the pattern does match:
	- If any members have a missing genotype the pattern match will be 'potential'.
##### Examples
| Patient | Father | Mother | Result    | Explanation                                                                                                                                      |
|---------|--------|--------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| 1       | 0      | 0      | true      | Patient is the only one with an alternative allele.                                                                                              |
| 1       | 0      | 1      | false     | Unaffected mother has the same genotype as the patient.                                                                                          |
| 1       | 0      | 2      | potential | Unaffected mother also has an alternative allele, but a different one than the patient, we do not know if those alleles match in pathogenicity.  |
| .       | 0      | 1      | potential | Patient missing allele can be anything, therefor the result is "potential".                                                                      |
| 1       | 0      | .      | potential | Mother missing allele can be anything; same as the patient alternative, reference, other alternative allele, therefor the result is "potential". |

## Running without pedigree file
If the tool runs without a ped file, all probands are assumed to be affected.
For variants on the X chromosome deploid genotypes are assumed to be female, single alleles are assumed to be male.

## Running without VEP inheritance mode annotations
If the VEP inheritance mode annotation is missing the tool still calculates all possible inheritance modes.
However, the actual matching on genes will obviously never yield a result.

## Compatible Inheritance modes
The VIP inheritance plugin adds a whole range of inheritance modes, however for matching purposes we can only use a subset: AD,AR,XL,XLD,XLR.

### Supported
| OMIM Inheritance*   | Annotation |
|---------------------|------------|
| X-LINKED DOMINANT   | XD         |
| X-LINKED RECESSIVE  | XR         |
| X-LINKED*           | XL         |
| AUTOSOMAL RECESSIVE | AR         |
| AUTOSOMAL DOMINANT  | AD         |
| Y-LINKED            | YL         |
| MITOCHONDRIAL       | MT         |
*: Please note that XL is matched by both XD and XR.

### Unsupported
| OMIM Inheritance*               | Annotation |
|---------------------------------|------------|
| PSEUDOAUTOSOMAL RECESSIVE       | PR         |
| PSEUDOAUTOSOMAL DOMINANT        | PD         |
| ISOLATED CASES                  | IC         |
| DIGENIC                         | DG         |
| DIGENIC RECESSIVE               | DGR        |
| DIGENIC DOMINANT                | DGD        |
| MULTIFACTORIAL                  | MF         |
| SOMATIC MUTATION                | SM         |
| SOMATIC MOSAICISM               | SMM        |
| INHERITED CHROMOSOMAL IMBALANCE | ICI        |
