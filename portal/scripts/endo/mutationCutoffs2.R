#!/usr/bin/Rscript --no-save
library(survival)

print("Determining Mutation Cutoffs...  Loading Packages...")

sink(file="/dev/null")
suppressMessages(library("pdfCluster"))
suppressMessages(library ("gdata"))
suppressMessages(library ("gplots"))
suppressMessages(library(ggplot2))
sink()

######################################################################################
# A Series of Analyses to Identify Mutation Rate Clusters in TCGA Endometrial Cancer
######################################################################################

######################################################################################
# Init Clinical Data Frame
######################################################################################
init <- function() {
	# Read in Unified Clinical File
	df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

	# Restrict to Sequenced Cases Only
	sub_df = subset(df, SEQUENCED=="Y")

	# Remove outliers
	# Case with exactly 1 mutation
	sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

	# Create new Rate Columns
	sub_df = transform(sub_df, COVERED_BASES = COVERED_BASES/1e+06)
	sub_df = transform(sub_df, SILENT_RATE=SILENT_MUTATION_COUNT/COVERED_BASES)
	sub_df = transform(sub_df, NON_SILENT_RATE=NON_SILENT_MUTATION_COUNT/COVERED_BASES)
	sub_df = transform(sub_df, TOTAL_SNV_RATE=TOTAL_SNV_COUNT/COVERED_BASES)
	sub_df = transform(sub_df, INDEL_RATE=INDEL_MUTATION_COUNT/COVERED_BASES)

	# Create new InDel Ratio Column
	sub_df = transform(sub_df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

	# Sort by NON_SILENT_RATE
	sub_df = sub_df[order (sub_df$NON_SILENT_RATE, decreasing=T),]

	return(sub_df)
}

######################################################################################
# Perform Kernel Density Estimate Clustering 
######################################################################################
kernel_density <-function (){
	# Plot Kernel Density Estimate of SILENT_RATE
	plot(density(log10(sub_df$NON_SILENT_RATE), bw="ucv"))

	# Perform Density Estimate Clustering on NON_SILENT_RATE
  cl <- pdfCluster(log10(sub_df$NON_SILENT_RATE), n.stage=25, hmult=0.35)
  
	# append cluster assignments to sub_df
	sub_df <- data.frame(sub_df, cl@clusters)
	plot(cl)
	return(sub_df)
}

# Create Plot of NON_SILENT_RATE + SILENT_RATE
plotSilentAndNonSilentRates <-function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, shape=factor(cl.clusters), data=sub_df)
	p = p + geom_point(aes(y=NON_SILENT_RATE), colour="blue", size=3) 
	p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=3) 
	p = p + opts(title="Mutation Rates:  Top 75 Cases")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
  p = p + xlim(0, 75)
	p
}

# Create Plot of NON_SILENT_RATE, size=INDEL_RATIO
plotNonSilentRateInDelSize <- function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, size=INDEL_RATIO, colour=factor(cl.clusters), shape=1)
	p = p + geom_point(aes(y=NON_SILENT_RATE))
  # NB:  Limits of scale are set; this may need to be manually tweaked in the future
	p = p + scale_size(to = c(1, 8), limits=c(0.0, 0.15))
	p = p + opts(title="Indel Ratios:  Top 75 Cases")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
  p = p + xlim(0, 75)
	p
}

# Create Plot of NON_SILENT_RATE, color=MSI
plotNonSilentRateMsiColour <-function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=MSI_STATUS, shape=factor(cl.clusters))
	p = p + geom_point(aes(y=NON_SILENT_RATE), size=3)
	p = p + opts(title="MSI Status:  Top 75 Cases")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
  p = p + xlim(0, 75)
	p
  
  t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=factor(MLH1_HYPERMETHYLATED), shape=factor(cl.clusters))
	p = p + geom_point(aes(y=NON_SILENT_RATE), size=3)
	p = p + opts(title="MLH1 Hypermethylation:  Top 75 Cases")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
  p = p + xlim(0, 75)
	p
}

# Get Summary Stats
calculateSummaryStats <-function(sub_df) {
	low_df = subset(sub_df, cl.clusters==1)
	high_df = subset(sub_df, cl.clusters==2)
	higher_df = subset(sub_df, cl.clusters==4)
  highest_df = subset(sub_df, cl.clusters==3)

  low = nrow(low_df)
  high = nrow(high_df)
	higher = nrow(higher_df)
  highest = nrow(highest_df)
	row0 = list(METRIC="NUM_SAMPLES", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

	low = mean(low_df$NON_SILENT_RATE)
	high = mean(high_df$NON_SILENT_RATE)
	higher = mean(higher_df$NON_SILENT_RATE)
  highest = mean(highest_df$NON_SILENT_RATE)
	row1 = list(METRIC="NON_SILENT_RATE", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

	low = mean(low_df$SILENT_RATE)
	high = mean(high_df$SILENT_RATE)
  higher = mean(higher_df$SILENT_RATE)
	highest = mean(highest_df$SILENT_RATE)
	row2 = list(METRIC="SILENT_RATE", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

	low = mean(low_df$TOTAL_SNV_COUNT)
	high = mean(high_df$TOTAL_SNV_COUNT)
  higher = mean(higher_df$TOTAL_SNV_COUNT)
	highest = mean(highest_df$TOTAL_SNV_COUNT)
	row3 = list(METRIC="TOTAL_SNV_COUNT", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)
  
  low = mean(low_df$INDEL_RATIO)
	high = mean(high_df$INDEL_RATIO)
  higher = mean(higher_df$INDEL_RATIO)
	highest = mean(highest_df$INDEL_RATIO)
	row4 = list(METRIC="INDEL_RATIO", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

  low = nrow(low_df[low_df$MSI_STATUS=="MSI-H",]) / nrow(low_df)
  high = nrow(high_df[high_df$MSI_STATUS=="MSI-H",]) / nrow(high_df)
  higher = nrow(higher_df[higher_df$MSI_STATUS=="MSI-H",]) / nrow(higher_df)
  highest = nrow(highest_df[highest_df$MSI_STATUS=="MSI-H",]) / nrow(highest_df)
	row5 = list(METRIC="MSI-H", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

  low = nrow(low_df[low_df$MSI_STATUS=="MSI-L",]) / nrow(low_df)
  high = nrow(high_df[high_df$MSI_STATUS=="MSI-L",]) / nrow(high_df)
  higher = nrow(higher_df[higher_df$MSI_STATUS=="MSI-L",]) / nrow(higher_df)
  highest = nrow(highest_df[highest_df$MSI_STATUS=="MSI-L",]) / nrow(highest_df)
  row6 = list(METRIC="MSI-L", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

  low = nrow(low_df[low_df$MLH1_HYPERMETHYLATED==1,]) / nrow(low_df)
  high = nrow(high_df[high_df$MLH1_HYPERMETHYLATED==1,]) / nrow(high_df)
  higher = nrow(higher_df[higher_df$MLH1_HYPERMETHYLATED==1,]) / nrow(higher_df)
  highest = nrow(highest_df[highest_df$MLH1_HYPERMETHYLATED==1,]) / nrow(highest_df)
  row7 = list(METRIC="MLH1 Hypermethylated", LOW=low, HIGH=high, HIGHER=higher, HIGHEST=highest)

	summary = rbind (data.frame(row0), data.frame(row1), data.frame(row2), data.frame(row3),
      data.frame(row4), data.frame(row5), data.frame(row6), data.frame(row7))
  options(scipen=999)
	textplot(summary, hadj=1, show.rownames=F, cex=1.0, halign="left", valign="top")
	title ("Mutation Rate Clusters Summary Stats")
}

mergeAndSaveUpdatedClinicalFile <- function(sub_df) {
	# Read in Original Clinical File
	df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

	temp_df = subset(sub_df, select=c("CASE_ID", "cl.clusters"))

	# Recalibrate cluster values
	temp_df = transform(temp_df, cl.clusters=as.character(cl.clusters))
	temp_df[temp_df$cl.clusters=="1",]$cl.clusters="1_LOW"
	temp_df[temp_df$cl.clusters=="2",]$cl.clusters="2_HIGH"
	temp_df[temp_df$cl.clusters=="3",]$cl.clusters="3_HIGHEST"
  temp_df[temp_df$cl.clusters=="4",]$cl.clusters="3_HIGHEST"

	merged = merge(df, temp_df, all.x=T)
	
	# Rename Column
	names(merged)[length(names(merged))] = "MUTATION_RATE_CLUSTER"
	
	# Then save out to new text file
	write.table(merged, file="~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt", quote=F, sep="\t", row.names=F)
}

survival <- function (sub_df) {
  # Encode DFS_STATUS_BOOLEAN
  # 0 = Disease Free
  # 1 = Recurred / Progressed
  sub_df = transform (sub_df, DFS_STATUS_BOOLEAN=NA)
  sub_df[sub_df$DFS_STATUS=="DiseaseFree",]$DFS_STATUS_BOOLEAN=0
  sub_df[sub_df$DFS_STATUS=="Recurred",]$DFS_STATUS_BOOLEAN=1
  dfs_surv = Surv (sub_df$DFS_MONTHS, sub_df$DFS_STATUS_BOOLEAN)
  dfs_surv_fit = survfit(dfs_surv ~ sub_df$cl.clusters)
  dfs_log_rank = survdiff (dfs_surv ~ sub_df$cl.clusters)

  #print(dfs_surv_fit)

  labels=c("Low", "High", "Highest", "Higher")
  colors=c("red", "blue", "green", "orange")
  plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
  legend ("topright", bty="n", labels, fill=colors)
  p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
  legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))
  
  sub_df = transform(sub_df, MUT_HIGHER_HIGHEST=0)
  sub_df[sub_df$cl.clusters==3,]$MUT_HIGHER_HIGHEST=1
  sub_df[sub_df$cl.clusters==4,]$MUT_HIGHER_HIGHEST=1

  dfs_surv = Surv (sub_df$DFS_MONTHS, sub_df$DFS_STATUS_BOOLEAN)
  dfs_surv_fit = survfit(dfs_surv ~ sub_df$MUT_HIGHER_HIGHEST)
  dfs_log_rank = survdiff (dfs_surv ~ sub_df$MUT_HIGHER_HIGHEST)

  #print(dfs_surv_fit)

  labels=c("Mut_High_Low", "Mut_Higher_Highest")
  colors=c("red", "blue", "green", "orange")
  plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
  legend ("topright", bty="n", labels, fill=colors)
  p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
  legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))
}

pdf("mutation_rates.pdf", width=9, height=7) 
sub_df = init()
sub_df = kernel_density()
calculateSummaryStats(sub_df)
plotSilentAndNonSilentRates(sub_df)
plotNonSilentRateInDelSize(sub_df)
plotNonSilentRateMsiColour(sub_df)
mergeAndSaveUpdatedClinicalFile(sub_df)
survival(sub_df)

garbage = dev.off()

print ("PDF report written to:  mutation_rates.pdf")
print ("Updated clinical file written to:  ucec_clinical_with_clusters_unified.txt")
