[![Build Status](https://travis-ci.org/molgenis/vip-inheritance-matcher.svg?branch=main)](https://travis-ci.org/github/molgenis/vip-inheritance-matcher)
[![Quality Status](https://sonarcloud.io/api/project_badges/measure?project=molgenis_vip-inheritance-matcher&metric=alert_status)](https://sonarcloud.io/dashboard?id=molgenis_vip-inheritance-matcher)
# vip inheritance matcher
annotates VCF samples with denovo and possible compound flags and matching inheritance modes and genes.

Input VCF file should be annotated with [Genmod models](http://moonso.github.io/genmod/) and the [VIP inheritance VEP plugin](https://github.com/molgenis/vip/tree/master/plugins/vep).

###Added Sample information
```
##FORMAT=<ID=VIC,Number=1,Type=Integer,Description="Inheritance Compound status.">
##FORMAT=<ID=VID,Number=1,Type=Integer,Description="Inheritance Denovo status.">
##FORMAT=<ID=VIG,Number=.,Type=String,Description="Genes with an inheritance match.">
##FORMAT=<ID=VIM,Number=1,Type=String,Description="Inheritance Match status.">
##FORMAT=<ID=VI,Number=.,Type=String,Description="An enumeration of possible inheritance modes.">
```

###Usage
```
usage: java -jar vcf-inheritance-matcher.jar -i <arg> [-o <arg>] [-pd
       <arg>] [-pb <arg>] [-f] [-d]
 -i,--input <arg>       Input VCF file (.vcf or .vcf.gz).
 -o,--output <arg>      Output VCF file (.vcf or .vcf.gz).
 -pd,--pedigree <arg>   Comma-separated list of pedigree files (.ped).
 -pb,--probands <arg>   Comma-separated list of proband sample
                        identifiers.
 -f,--force             Override the output file if it already exists.
 -d,--debug             Enable debug mode (additional logging).

usage: java -jar vcf-inheritance-matcher.jar -v
 -v,--version   Print version.
```

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
*: Please note that XL is matched by both XD and XR from the Genmod annotation.

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
