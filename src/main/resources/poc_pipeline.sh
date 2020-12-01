#!/bin/bash
INPUT=$1
PEDIGREE=$2

rm -R workdir
mkdir ./workdir

../ensembl-vep/vep --i "${INPUT}" --format vcf -o ./workdir/vep.out.vcf --force_overwrite --species homo_sapiens --vcf --cache --offline --pick_allele --assembly GRCh37 --use_given_ref -fa ../.vep/Homo_sapiens.GRCh37.75.dna.primary_asse$
bcftools +split-vep ./workdir/vep.out.vcf -c SYMBOL >./workdir/splitted.vcf

genmod models ./workdir/splitted.vcf -f "${PEDIGREE}" -k SYMBOL -p 1 >./workdir/genmod.vcf

java -jar vcf-inheritance-matcher.jar -i ./workdir/genmod.vcf -o ./workdir/matched.vcf

bcftools annotate -x INFO/SYMBOL,INFO/Compounds,INFO/ModelScore,INFO/GeneticModels -o ./workdir/output_inheritance.vcf ./workdir/matched.vcf

java -jar vcf-report.jar -i ./workdir/output_inheritance.vcf
