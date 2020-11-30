# vip inheritance matcher
annotates VCF samples with denovo and possible compound flags and matching inheritance modes and genes.

###Added Sample information
```
##FORMAT=<ID=CMP,Number=1,Type=Integer,Description="Possible compound status for AR inheritance modes, 1 = true, 0 = false.">
##FORMAT=<ID=DNV,Number=1,Type=Integer,Description="Denovo status, 1 = true, 0 = false.">
##FORMAT=<ID=GENES,Number=.,Type=String,Description="Genes for which inheritance modes of the sample and gene match.">
##FORMAT=<ID=MATCH,Number=1,Type=String,Description="Does inheritance match for sample and genes, 1 = true, 0 = false.">
##FORMAT=<ID=MDS,Number=.,Type=String,Description="Predicted inheritance modes.">
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