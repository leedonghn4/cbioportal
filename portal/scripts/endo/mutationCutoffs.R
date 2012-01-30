#!/usr/bin/Rscript --no-save

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
	# Plot Kernel Density Estimate of NON_SILENT RATE
	plot(density(log10(sub_df$NON_SILENT_RATE)))
	abline(v=2.0,col="red",lty=3)
	abline(v=.65, col="red", lty=3)

	# Plot Kernel Density Estimate of SILENT_RATE
	plot(density(log10(sub_df$SILENT_RATE)))

	# Perform Density Estimate Clustering on NON_SILENT_RATE
	cl <- pdfCluster(log10(sub_df$NON_SILENT_RATE), n.stage=55, hmult=0.5)

	# append cluster assignments to sub_df
	sub_df <- data.frame(sub_df, cl@clusters)
	plot(cl)
	return(sub_df)
}

# Create Plot of NON_SILENT_RATE + SILENT_RATE
plotSilentAndNonSilentRates <-function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df)
	p = p + geom_point(aes(y=NON_SILENT_RATE), colour="blue", size=3) 
	p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=3) 
	p = p + opts(title="Mutation Rates")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
	p
}

# Create Plot of NON_SILENT_RATE, size=INDEL_RATIO
plotNonSilentRateInDelSize <- function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, size=INDEL_RATIO, shape=1)
	p = p + geom_point(aes(y=NON_SILENT_RATE))
	p = p + scale_size(to = c(1, 8))
	p = p + opts(title="Mutation Rates")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
	p
}

# Create Plot of NON_SILENT_RATE, color=MSI
plotNonSilentRateMsiColour <-function(sub_df) {
	t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
	t1 <- seq(from=t0[1], to=t0[2]) 
	p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=MSI_STATUS)
	p = p + geom_point(aes(y=NON_SILENT_RATE), size=3)
	p = p + opts(title="Mutation Rates")
	p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
	p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
	p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
	p
}

# Get Summary Stats
calculateSummaryStats <-function(sub_df) {
	low = subset(sub_df, cl.clusters==1)
	high = subset(sub_df, cl.clusters==2)
	highest = subset(sub_df, cl.clusters==3)

	l = mean(low$NON_SILENT_RATE)
	h = mean(high$NON_SILENT_RATE)
	u = mean(highest$NON_SILENT_RATE)
	row0 = list(METRIC="NON_SILENT_RATE", LOW=l, HIGH=h, HIGHEST=u)

	l = mean(low$SILENT_RATE)
	h = mean(high$SILENT_RATE)
	u = mean(highest$SILENT_RATE)
	row1 = list(METRIC="SILENT_RATE", LOW=l, HIGH=h, HIGHEST=u)

	l = mean(low$TOTAL_SNV_COUNT)
	h = mean(high$TOTAL_SNV_COUNT)
	u = mean(highest$TOTAL_SNV_COUNT)
	row2 = list(METRIC="TOTAL_SNV_COUNT", LOW=l, HIGH=h, HIGHEST=u)

	l = nrow(low)
	h = nrow(high)
	u = nrow(highest)
	row3 = list(METRIC="NUM_SAMPLES", LOW=l, HIGH=h, HIGHEST=u)

	summary = rbind (data.frame(row0), data.frame(row1), data.frame(row2), data.frame(row3))
	textplot(summary, hadj=1, show.rownames=F, cex=1.3, halign="left", valign="top")
	title ("Mutation Rate Clusters Summary Stats")
}

mergeAndSaveUpdatedClinicalFile <- function(sub_df) {
	# Read in Original Clinical File
	df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

	temp_df = subset(sub_df, select=c("CASE_ID", "cl.clusters"))
	merged = merge(df, temp_df)
	
	# Rename Column
	names(merged)[length(names(merged))] = "MUTATION_RATE_CLUSTER"
	
	# Then save out to new text file
	write.table(merged, file="~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt", quote=F, sep="\t")
}

pdf("mutation_rates.pdf", width=9, height=7) 
sub_df = init()
sub_df = kernel_density()
calculateSummaryStats(sub_df)
plotSilentAndNonSilentRates(sub_df)
plotNonSilentRateInDelSize(sub_df)
plotNonSilentRateMsiColour(sub_df)
mergeAndSaveUpdatedClinicalFile(sub_df)

garbage = dev.off()

print ("PDF report written to:  mutation_rates.pdf")
print ("Updated clinical file written to:  ucec_clinical_with_clusters_unified.txt")
