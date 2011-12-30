# First, verify if all symbols in the sample genesets are latest
./verifyGeneSets.pl $CGDS_DATA_HOME/human_genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $CGDS_DATA_HOME/human_genes.txt

# Load up Cancer Types
./importTypesOfCancer.pl $CGDS_DATA_HOME/cancers.txt

# Load up Sanger Cancer Gene Census
./importSangerCensus.pl ../sample_data/genes/sanger_gene_census.txt

# Load up the Endometrioid (UCEC) Meta Data File
./importCancerStudy.pl ~/endo/ucec.txt

# Imports All Case Lists and Clinical Data
./importCaseList.pl ~/endo/case_lists
./importClinicalData.pl ~/endo/UCEC.clinical.txt

# Imports Mutation Data
./importProfileData.pl --data ~/endo/UCEC.maf.annotated --meta ~/endo/meta_mutations_MAF.txt --dbmsAction clobber

# Imports Copy Number Data
#./importProfileData.pl --data $CGDS_STAGING_HOME/ucec/data_CNA.txt --meta $CGDS_STAGING_HOME/ucec/meta_CNA.txt --dbmsAction clobber

# Import Gene Sets
#./importGeneSets.pl ../sample_data/gene_sets/c2.cp.v3.0.entrez.gmt

# Import MutSig
./importGeneSets.pl ~/endo/UCEC.sig_gene_sets.txt 