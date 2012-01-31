#!/usr/bin/Rscript --no-save

# Focus on MSI-L:  3_HIGHEST v. 2_HIGH
compareMsiLow1 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="3_HIGHEST" | df_sub$MUTATION_RATE_CLUSTER=="2_HIGH")
	df_temp <-transform(df_temp, MSI_LOW=0)
	df_temp[df_temp$MSI_STATUS=="MSI-L",]$MSI_LOW=1
	t = table(df_temp$MSI_LOW, df_temp$MUTATION_RATE_CLUSTER, exclude=c("1_LOW"))

	percentMsiLowInHighestCluster = round(prop.table(t,2)[2,2], 3) * 100.0
	percentMsiLowInHighCluster = round(prop.table(t,2)[2,1], 3) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 4)
	summary = paste(percentMsiLowInHighestCluster, 
	   "% of cases in the highest mutation rate cluster are MSI-L, compared to ", 
	   percentMsiLowInHighCluster, "% of cases in the high mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary)
	cat("\n") 
}

# Focus on MSI-L:  3_HIGHEST v. 3_LOW
compareMsiLow2 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="3_HIGHEST" | df_sub$MUTATION_RATE_CLUSTER=="1_LOW")
	df_temp <-transform(df_temp, MSI_LOW=0)
	df_temp[df_temp$MSI_STATUS=="MSI-L",]$MSI_LOW=1
	t = table(df_temp$MSI_LOW, df_temp$MUTATION_RATE_CLUSTER, exclude=c("2_HIGH"))

	percentMsiLowInHighestCluster = round(prop.table(t,2)[2,2], 3) * 100.0
	percentMsiLowInLowCluster = round(prop.table(t,2)[2,1], 3) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 6)
	summary = paste(percentMsiLowInHighestCluster, 
	   "% of cases in the highest mutation rate cluster are MSI-L, compared to ", 
	   percentMsiLowInLowCluster, "% of cases in the low mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

# Focus on MSI-H:  3_HIGHEST v. 2_HIGH
compareMsiHigh1 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="3_HIGHEST" | df_sub$MUTATION_RATE_CLUSTER=="2_HIGH")
	df_temp <-transform(df_temp, MSI_HIGH=0)
	df_temp[df_temp$MSI_STATUS=="MSI-H",]$MSI_HIGH=1
	t = table(df_temp$MSI_HIGH, df_temp$MUTATION_RATE_CLUSTER, exclude=c("1_LOW"))

	percentMsiHighInHighestCluster = round(prop.table(t,2)[2,2], 3) * 100.0
	percentMsiHighInHighCluster = round(prop.table(t,2)[2,1], 3) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 6)
	summary = paste(percentMsiHighInHighestCluster, 
	   "% of cases in the highest mutation rate cluster are MSI-H, compared to ", 
	   percentMsiHighInHighCluster, "% of cases in the high mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

options(warn=-1)
# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Filter:
# 1. only sequenced cases
# 2. only cases that have a MUTATION_RATE_CLUSTER
# 3. only cases for which we have definitive MSI results
df_sub = subset(df, SEQUENCED=="Y")
df_sub = subset (df_sub, MUTATION_RATE_CLUSTER != "NA")
df_sub <- subset(df_sub, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

t = table(df_sub$MSI_STATUS, df_sub$MUTATION_RATE_CLUSTER, exclude=c("Indeterminant", "Not Done"))

print(prop.table(t, 2))

compareMsiLow1(df_sub)
compareMsiLow2(df_sub)
compareMsiHigh1(df_sub)