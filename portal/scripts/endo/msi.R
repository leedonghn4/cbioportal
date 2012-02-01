#!/usr/bin/Rscript --no-save

sink(file="/dev/null")
suppressMessages(library (ggplot2))
suppressMessages(library(gridExtra))
sink()

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

# Focus on MSI-H:  2_HIGH v. 1_LOW
compareMsiHigh2 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="2_HIGH" | df_sub$MUTATION_RATE_CLUSTER=="1_LOW")
	df_temp <-transform(df_temp, MSI_HIGH=0)
	df_temp[df_temp$MSI_STATUS=="MSI-H",]$MSI_HIGH=1
	t = table(df_temp$MSI_HIGH, df_temp$MUTATION_RATE_CLUSTER, exclude=c("3_HIGHEST"))

	percentMsiHighInHighCluster = round(prop.table(t,2)[2,2], 3) * 100.0
	percentMsiHighInLowCluster = round(prop.table(t,2)[2,1], 3) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 40)
	summary = paste(percentMsiHighInHighCluster, 
	   "% of cases in the high mutation rate cluster are MSI-H, compared to ", 
	   percentMsiHighInLowCluster, "% of cases in the low mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

# Focus on MSS:  2_HIGH v. 1_LOW
compareMss <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="2_HIGH" | df_sub$MUTATION_RATE_CLUSTER=="1_LOW")
	df_temp <-transform(df_temp, MSS=0)
	df_temp[df_temp$MSI_STATUS=="MSS",]$MSS=1
	t = table(df_temp$MSS, df_temp$MUTATION_RATE_CLUSTER, exclude=c("3_HIGHEST"))

	percentMssInHighCluster = round(prop.table(t,2)[2,2], 4) * 100.0
	percentMssInLowCluster = round(prop.table(t,2)[2,1], 4) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 40)
	summary = paste(percentMssInHighCluster, 
	   "% of cases in the high mutation rate cluster are MSS, compared to ", 
	   percentMssInLowCluster, "% of cases in the low mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

plotMsi <- function(df_sub) {
	p = ggplot(df_sub, aes(x = factor(1), fill = factor(MSI_STATUS))) + geom_bar(width = 1)
	p= p + opts(title = "MSI Status:  By Mutation Rate Cluster") 
	p=p+facet_grid(facets=. ~ MUTATION_RATE_CLUSTER)
	p=p+xlab("") + opts(axis.text.x = theme_blank(), axis.ticks = theme_blank())
	p
}

plotMlh1Hypermethylation <- function(df_sub) {
	p = ggplot(df_sub, aes(x = factor(1), fill = factor(MLH1_HYPERMETHYLATED))) + geom_bar(width = 1)
	p= p + opts(title = "MLH1 Hypermethylation:  By Mutation Rate Cluster") 
	p=p+facet_grid(facets=. ~ MUTATION_RATE_CLUSTER)
	p=p+xlab("") + opts(axis.text.x = theme_blank(), axis.ticks = theme_blank())
	p
}

# Focus on MLH1:  High v. Low
compareMlh1_1 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="2_HIGH" | df_sub$MUTATION_RATE_CLUSTER=="1_LOW")
	t = table(df_temp$MLH1_HYPERMETHYLATED, df_temp$MUTATION_RATE_CLUSTER, exclude=c("3_HIGHEST"))

	percentMlh1InHighCluster = round(prop.table(t,2)[2,2], 4) * 100.0
	percentMlh1InLowCluster = round(prop.table(t,2)[2,1], 4) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 40)
	summary = paste(percentMlh1InHighCluster, 
	   "% of cases in the high mutation rate cluster are MLH1 hypermethylated, compared to ", 
	   percentMlh1InLowCluster, "% of cases in the low mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

# Focus on MLH1:  Highest v. High
compareMlh1_2 <- function(df_sub) {
	df_temp <- subset (df_sub, df_sub$MUTATION_RATE_CLUSTER=="3_HIGHEST" | df_sub$MUTATION_RATE_CLUSTER=="2_HIGH")
	t = table(df_temp$MLH1_HYPERMETHYLATED, df_temp$MUTATION_RATE_CLUSTER, exclude=c("1_LOW"))

	percentMlh1InHighestCluster = round(prop.table(t,2)[2,2], 4) * 100.0
	percentMlh1InHighCluster = round(prop.table(t,2)[2,1], 4) * 100.

	f = fisher.test(t)
	p_value = round(f$p.value, 5)
	summary = paste(percentMlh1InHighestCluster, 
	   "% of cases in the highest mutation rate cluster are MLH1 hypermethylated, compared to ", 
	   percentMlh1InHighCluster, "% of cases in the high mutation rate cluster. p-value:  ",
	   p_value, ".", sep="")
  	cat("--------------\n")
	cat(summary) 
	cat("\n") 
}

options(warn=-1)

# Start PDF
pdf("msi.pdf", width=9, height=7)

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Filter:
# 1. only sequenced cases
# 2. only cases that have a MUTATION_RATE_CLUSTER
# 3. only cases for which we have definitive MSI results
df_sub = subset(df, SEQUENCED=="Y")
df_sub = subset (df_sub, MUTATION_RATE_CLUSTER != "NA")

plotMlh1Hypermethylation(df_sub)
compareMlh1_1(df_sub)
compareMlh1_2(df_sub)

df_sub <- subset(df_sub, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

t = table(df_sub$MSI_STATUS, df_sub$MUTATION_RATE_CLUSTER, exclude=c("Indeterminant", "Not Done"))
print(prop.table(t, 2))
plotMsi(df_sub)

compareMsiLow1(df_sub)
compareMsiLow2(df_sub)
compareMsiHigh1(df_sub)
compareMsiHigh2(df_sub)
compareMss(df_sub)

cat ("\nPlots written to msi.pdf\n")

garbage = dev.off()
