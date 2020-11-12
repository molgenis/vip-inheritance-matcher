# vip-inheritance-matcher
annotates VCF samples with a medelian violation indication and matching inheritance modes with a possible compound indication.

###Example
```
##FORMAT=<ID=IHM,Number=1,Type=String,Description="Inheritance mode and compound information for this sample in the format: GENE1|MODE|COMPOUND,GENE2|MODE|COMPOUND">
##FORMAT=<ID=MV,Number=1,Type=Integer,Description="Indication if the variant is a mendelian violation for this sample.">
#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	Patient	Mother	Father
1	1	.	C	T	.	PASS	CSQ=C|stop_gained||GENE1|	GT:DP:IHM:MV	1/1:99:GENE1|AR|0:0	0/1:99	0/1:99
```
The variant on chromosome 1 position 1 has an autosomal recessive (AR) pattern for Gene1, it is a not a possible compound and no mendelian violation.

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