#!/usr/bin/Rscript --no-save
# Load ggplots2
library(ggplot2)

#####################################################################
# A Series of Graphs Exploring Hypermutation in Endometrical Cancer
#####################################################################

######################################################################################
# Init Clinical Data Frame
######################################################################################
init <- function() {
	# Read in Unified Clinical File
	df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

	# Create new SUBTYPE Column that has Shorter Labels
	df = transform(df, SUBTYPE="NA")
	df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3", "Mixed", "Serous"))
	df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endo-Grade-1"
	df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endo-Grade-2"
	df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endo-Grade-3"
	df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
	df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

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

# Start PDF
pdf("report.pdf", width=9, height=7) 

sub_df = init()

# Create Plot of SNV Rates, Log Scale
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df)
p = p + geom_point(aes(y=NON_SILENT_RATE), colour="blue", size=2) 
p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=2) 
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p

# Create Plot of SNV Rates, Color Coded by Mutation Categories, Log Scale
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, geom="point", colour=MUTATION_RATE_CLUSTER)
p = p + opts(title="Mutation Clusters")
p = p + ylab("log10(Non-Silent Rate)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p = p + geom_rug(aes(y = NULL)) 
p

# Create Plot of SNV Rates, Color Coded by MSI-Status, Log Scale
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, geom="point", colour=MSI_STATUS)
p = p + opts(title="Mutation Clusters")
p = p + ylab("log10(Non-Silent Rate)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p = p + scale_colour_brewer(type="qual", palette=6)
p = p + geom_rug(aes(y = NULL)) 
p

# Create Plot of SNV Rates, Color Coded by MLH1 Hypermethylation, Log Scale
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, geom="point", colour=factor(MLH1_HYPERMETHYLATED))
p = p + opts(title="Mutation Clusters")
p = p + ylab("log10(Non-Silent Rate)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p = p + geom_rug(aes(y = NULL)) 
p

# Create Plot of SNV Rates, Node Size is Proportional to INDEL_RATIO
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, geom="point", size=INDEL_RATIO, shape=1)
p = p + opts(title="Mutation Clusters")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + ylab("log10(Non-Silent Rate)")
p = p + scale_size(to = c(2, 10))
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p

# Compare INDEL_RATIO in MUTATION_RATE_CLUSTER
kt = kruskal.test(INDEL_RATIO ~ factor(MUTATION_RATE_CLUSTER), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), INDEL_RATIO))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("InDel Ratio") 
the_title = paste("InDel Ratios Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p

# Compare INDEL_RATE in MUTATION_RATE_CLUSTER
kt = kruskal.test(INDEL_RATE ~ factor(MUTATION_RATE_CLUSTER), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), INDEL_RATE))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Indel Rate") 
the_title = paste("InDel Rate Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p


pw = pairwise.wilcox.test(sub_df$INDEL_RATIO, sub_df$MUTATION_RATE_CLUSTER, p.adj = "bonf")

# Compare AGE in MUTATION_RATE_CLUSTER
kt = kruskal.test(age_at_initial_pathologic_diagnosis ~ factor(MUTATION_RATE_CLUSTER), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), age_at_initial_pathologic_diagnosis))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Clusters") 
p = p + ylab("Age at Diagnosis") 
the_title = paste("Age Across Mutation Clusters\nKruskal–Wallis:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p

########################################################################
# Restrict to Cases that have CNA and Sequencing Data
sub_df = subset(sub_df, sub_df$SEQUENCED_AND_GISTIC=="Y")

# Sort by CNA_ALTERED_1
sub_df = sub_df[order (sub_df$CNA_ALTERED_1, decreasing=T),] 

# Create Plot of CNA v. Mutations
# First, determine correlation
c = cor(sub_df$TOTAL_SNV_COUNT, sub_df$CNA_ALTERED_1, method="spearman")
title = paste("Scatter Plot of CNA v. Mutation\nSpearman Correlation:  ", signif(c, 4))
p = qplot(CNA_ALTERED_1, TOTAL_SNV_COUNT, data=sub_df, geom="point")
p = p + xlab("# of Genes Altered by CNA")
p = p + ylab("log10(Total # of SNVs)")
p = p + opts (title=title)
p = p + scale_y_log10() 
p = p + geom_smooth(method="lm")
p

# End PDF 
dev.off() 
