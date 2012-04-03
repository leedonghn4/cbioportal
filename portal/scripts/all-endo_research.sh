# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl ../sample_data/genes/human_genes.txt

# Load up All Cancer Types
./importTypesOfCancer.pl ../sample_data/cancers.txt

# Load up Sanger Cancer Gene Census
./importSangerCensus.pl ../sample_data/genes/sanger_gene_census.txt

# Load up the Endometrioid (UCEC) Meta Data File
./importCancerStudy.pl ~/SugarSync/endo/data/ucec.txt

# Imports All Case Lists and Clinical Data
./importCaseList.pl ~/SugarSync/endo/data/out/case_lists
./importClinicalData.pl ~/SugarSync/endo/data/out/ucec_clinical_unified.txt

# Imports Mutation Data
./importProfileData.pl --data ~/SugarSync/endo/data/UCEC_03_18_2012.maf.annotated --meta ~/SugarSync/endo/data/meta_mutations_MAF.txt --dbmsAction clobber

# Imports Copy Number Data
./importProfileData.pl --data ~/SugarSync/endo/data/data_CNA.txt --meta ~/SugarSync/endo/data/meta_CNA.txt --dbmsAction clobber

# Imports mRNA Z-Scores Data
./importProfileData.pl --data ~/SugarSync/endo/data/data_mRNA_median_Zscores.txt --meta ~/SugarSync/endo/data/meta_mRNA_median_Zscores.txt --dbmsAction clobber

# Import Gene Sets
#./importGeneSets.pl ../sample_data/gene_sets/c2.cp.v3.0.entrez.gmt

# Import MutSig
./importGeneSets.pl ~/SugarSync/endo/data/UCEC.sig_gene_sets.txt 
