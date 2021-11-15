[![Build Status](https://app.travis-ci.com/molgenis/vip-inheritance-matcher.svg?branch=main)](https://app.travis-ci.com/molgenis/vip-inheritance-matcher)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-inheritance-matcher&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-inheritance-matcher)
# Variant Interpretation Pipeline - Inheritance Matcher
annotates VCF samples with denovo and possible compound flags and matching inheritance modes and genes.

Input VCF file should contain single ALT alleles per line and be annotated VEP.
Input should be annotated with [VIP inheritance VEP plugin](https://github.com/molgenis/vip/tree/master/plugins/vep) For full functionality.

###Added Sample information
```
##FORMAT=<ID=VI,Number=.,Type=String,Description="An enumeration of possible inheritance modes.">
##FORMAT=<ID=VIC,Number=1,Type=String,Description="List of possible compounds.">
##FORMAT=<ID=VID,Number=1,Type=Integer,Description="Inheritance Denovo status.">
##FORMAT=<ID=VIG,Number=.,Type=String,Description="Genes with an inheritance match.">
##FORMAT=<ID=VIM,Number=1,Type=Integer,Description="Inheritance Match status.">
##FORMAT=<ID=VIS,Number=.,Type=String,Description="An enumeration of possible sub inheritance modes like e.g. compound, non penetrance.">
```

###Usage
```
usage: java -jar vcf-inheritance-matcher.jar -i <arg> [-o <arg>] [-pd
       <arg>] [-pb <arg>] [-np <arg>] [-c] [-f] [-d]
 -i,--input <arg>            Input VCF file (.vcf or .vcf.gz).
 -o,--output <arg>           Output VCF file (.vcf or .vcf.gz).
 -pd,--pedigree <arg>        Comma-separated list of pedigree files
                             (.ped).
 -pb,--probands <arg>        Comma-separated list of proband sample
                             identifiers.
 -np,--nonpenetrance <arg>   File containing a list of non penetrance
                             genes (.tsv), first column is assumed to
                             contain the genes.
 -c,--annotateCompounds      Annotate variant/sample combinations with a
                             compound with the key of the other variant.
 -f,--force                  Override the output file if it already
                             exists.
 -d,--debug                  Enable debug mode (additional logging).
```

###Inheritance mode rules
Possible inheritance modes are calculated on the following rules:
####AR:
- Affected samples have to be homozygote ALT.
- Unaffected samples cannot be homozygous ALT.
####AR compound:
#####For unphased data:
- Affected samples need to have both variants.
- Unaffected samples cannot have both variants.
#####For phased data:
- Affected samples need to have both variants on different alleles.
- Unaffected samples cannot have both variants on different alleles, however they can have both variants on the same alleles..
####AD:
- Affected samples have to carry the ALT allele.
Unaffected samples have to be homozygous REF.
####AD non penetrant:
- Affected samples have to carry the ALT allele.
- Unaffected samples have to be homozygous REF, unless the gene on which the variant lies is also on the provided non-penetrance list.
####XLD:
- Affected samples have to have at least one ALT allele.
- Male unaffected patients cannot have the ALT allele, female unaffected samples can have a single ALT allele due to X inactivation.
####XLR:
- Female affected samples have to be homozygous ALT, male affected patients have to be homozygous ALT or have only the ALT allele.
- Female unaffected samples cannot be homozygous ALT, males cannot be homozygous ALT and connot have only the REF allele.
####XL:
- If the variant is XLD or XLR it is also considered XL.
####Denovo:
#####On regular chromosomes:
- Variant are considered denovo if one of the ALT alleles of the proband is not inherited from a parent.
#####On the X chromosome: 
- For male probands variants are considered denovo if mother does not have the ALT allele.
- For female probands variants are considered denovo following the same rules as for the other chromosomes.

###Running without pedigree file
If the tool runs without a ped file, all probands are assumed to be affected.
For variants on the X chromosome deploid genotypes are assumed to be female, single alleles are assumed to be male.

###Running without VEP inheritance mode annotations
If the VEP inheritance mode annotation is missing the tool still calculates all possible inheritance modes.
However the actual matching on genes will obviously never yield a result.

###Compatible Inheritance modes
The VIP inheritance plugin adds a whole range of inheritance modes, however for matching purposes we can only use these that are also used by Genmod.

####Supported
|OMIM Inheritance*|Annotation|
|---|---|
|X-LINKED DOMINANT|XD|
|X-LINKED RECESSIVE|XR|
|X-LINKED*|XL|
|AUTOSOMAL RECESSIVE|AR|
|AUTOSOMAL DOMINANT|AD|
*: Please note that XL is matched by both XD and XR.

#### Unsupported
|OMIM Inheritance*|Annotation|
|---|---|
|Y-LINKED|YL|
|PSEUDOAUTOSOMAL RECESSIVE|PR|
|PSEUDOAUTOSOMAL DOMINANT|PD|
|ISOLATED CASES|IC|
|DIGENIC|DG|
|DIGENIC RECESSIVE|DGR|
|DIGENIC DOMINANT|DGD|
|MITOCHONDRIAL|MT|
|MULTIFACTORIAL|MF|
|SOMATIC MUTATION|SM|
|SOMATIC MOSAICISM|SMM|
|INHERITED CHROMOSOMAL IMBALANCE|ICI|
