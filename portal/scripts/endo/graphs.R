# Load ggplots2
library(ggplot2)

# Read in Unified Clinical File

df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Make CNA Clusters into Factors, instead of Ints
df = transform (df, CNA_CLUSTER=as.factor(CNA_CLUSTER))

# Create new Total Mutation Count
df = transform(df, TOTAL_SNV_COUNT=SILENT_MUTATION_COUNT+NON_SILENT_MUTATION_COUNT)

# Create new InDel Ratio Column
df = transform(df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

# Restrict to Cases that have CNA and Sequencing Data
sub_df = subset(df, df$SEQUENCED_AND_GISTIC=="Y")

# Sort by CNA_ALTERED_1
sub_df = sub_df[order (sub_df$CNA_ALTERED_1, decreasing=T),] 

# Create Plot of Extent of CNA Alterations, Color-Coded by CNA Clusters
qplot(1:nrow(sub_df), CNA_ALTERED_1, data=sub_df, geom="point", colour=CNA_CLUSTER,
	xlab="All Cases with Sequence and CNA Data", ylab="# of Genes Altered by CNA",
	main="Association between Extent of Copy Number Alteration and Cluster Assignments")

# Sort by TOTAL_MUTATION_COUNT
sub_df = sub_df[order (sub_df$TOTAL_SNV_COUNT, decreasing=T),]

# Create Plot of SNV Rates, Color-Coded by CNA Clusters
qplot(1:nrow(sub_df), log(TOTAL_SNV_COUNT), data=sub_df, geom="point", colour=CNA_CLUSTER,
	xlab="All Cases with Sequence and CNA Data", ylab="log(Total # of SNVs)",
	main="Mutation Rates, Color-Coded by CNA Clusters")
	
# Create Plot of SNV Rates, Color-Coded by MSI-Status
qplot(1:nrow(sub_df), log(TOTAL_SNV_COUNT), data=sub_df, geom="point", colour=MSI_STATUS,
	xlab="All Cases with Sequence and CNA Data", ylab="log(Total # of SNVs)",
	main="Mutation Rates, Color-Coded by MSI-Status")

# Create Plot of SNV Rates, Color-Coded by MSI-Status, and Node Size Proportional to INDEL_RATIO
qplot(1:nrow(sub_df), log(TOTAL_SNV_COUNT), data=sub_df, geom="point", colour=MSI_STATUS,
	size=INDEL_RATIO,
	xlab="All Cases with Sequence and CNA Data", ylab="log(Total # of SNVs)",
	main="Mutation Rates, Color-Coded by MSI-Status")+geom_vline(xintercept = 22, linetype=2)+geom_vline(xintercept=78, linetype=2)

# Create Plot of CNA Mutations
# First, determine correlation
c = cor(log(sub_df$TOTAL_SNV_COUNT), sub_df$CNA_ALTERED_1, method="spearman")
title = paste("Scatter Plot of CNA v. Mutation\nSpearman Correlation:  ", signif(c, 4))
qplot(CNA_ALTERED_1, log(TOTAL_SNV_COUNT), data=sub_df, geom="point",
	xlab="# of Genes Altered by CNA", ylab="log(Total # of SNVs)",
	main=title) + geom_smooth(method="lm")
	
# Create Plot of CNA v. Mutations, Color Coded by CNA Clusters
qplot(CNA_ALTERED_1, log(TOTAL_SNV_COUNT), data=sub_df, geom="point", colour=CNA_CLUSTER,
	xlab="# of Genes Altered by CNA", ylab="log(Total # of SNVs)",
	main="Scatter Plot of CNA v. Mutation, Color-Coded by CNA Clusters")

# Create Plot of CNA v. Mutations, Color Coded by CNA Clusters;  Node Size = INDEL_RATIO	
qplot(CNA_ALTERED_1, log(TOTAL_SNV_COUNT), data=sub_df, geom="point", colour=CNA_CLUSTER,
	size=INDEL_RATIO,
	xlab="# of Genes Altered by CNA", ylab="log(Total # of SNVs)",
	main="Scatter Plot of CNA v. Mutation, Color-Coded by CNA Clusters")