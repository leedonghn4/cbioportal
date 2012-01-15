#!/usr/bin/Rscript --no-save
library(gplots)
library(gridExtra)

# Read in Unified Clinical File
clin_df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Read in MSI File
msi_df = read.delim("~/SugarSync/endo/data/out/msi_out.txt")

# Merge together
merged = merge (clin_df, msi_df)

# Extract a smaller df
merged = subset(merged, MSI_STATUS=="MSI-L" | MSI_STATUS=="MSI-H")

sub_df1 = subset(merged, merged$SEQUENCED=="Y",
	select=c(CASE_ID, BAT40, BAT26, BAT25, D17S250, TGFBII, D5S346, D2S123, PentaD, PentaE, MSI_STATUS, MUTATION_RATE_CATEGORY))

sub_df2 = subset(sub_df1, MSI_STATUS != "Indeterminant" & MSI_STATUS != "Not Done", 
	select=c(BAT40, BAT26, BAT25, D17S250, TGFBII, D5S346, D2S123, PentaD, PentaE))
sub_df3 = subset(sub_df1, MSI_STATUS != "Indeterminant" & MSI_STATUS != "Not Done",
	select=c("CASE_ID", "MSI_STATUS", "MUTATION_RATE_CATEGORY"))

sub_df3 = transform(sub_df3, MSS=0)
sub_df3[sub_df3$MSI_STATUS=="MSS",]$MSS=1
sub_df3 = transform(sub_df3, MSI_L=0)
sub_df3[sub_df3$MSI_STATUS=="MSI-L",]$MSI_L=1
sub_df3 = transform(sub_df3, MSI_H=0)
sub_df3[sub_df3$MSI_STATUS=="MSI-H",]$MSI_H=1
sub_df3 = transform(sub_df3, MUT_HIGHEST=0)
sub_df3[sub_df3$MUTATION_RATE_CATEGORY=="1_HIGHEST",]$MUT_HIGHEST=1
sub_df3 = transform(sub_df3, MUT_HIGH=0)
sub_df3[sub_df3$MUTATION_RATE_CATEGORY=="2_HIGH",]$MUT_HIGH=1
sub_df3 = transform(sub_df3, MUT_LOW=0)
sub_df3[sub_df3$MUTATION_RATE_CATEGORY=="3_LOW",]$MUT_LOW=1

sub_df4 = subset(sub_df3, select=c("MSS", "MSI_L", "MSI_H", "MUT_HIGHEST", "MUT_HIGH", "MUT_LOW"))

m = as.matrix (sub_df2)
rownames(m) = sub_df3$CASE_ID
m = t(m)
heatmap_plus(m, addvar = sub_df4)