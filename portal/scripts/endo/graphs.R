#!/usr/bin/Rscript --no-save
# Load ggplots2
library(ggplot2)

# Start PDF
pdf("report.pdf", width=9, height=7) 

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Make CNA Clusters into Factors, instead of Ints
df = transform (df, CNA_CLUSTER=as.factor(CNA_CLUSTER))

# Create new Total Mutation Count
df = transform(df, TOTAL_SNV_COUNT=SILENT_MUTATION_COUNT+NON_SILENT_MUTATION_COUNT)

# Create new InDel Ratio Column
df = transform(df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

# Create new MUTATION_RATE_CATEGORY Column
df = transform(df, MUTATION_RATE_CATEGORY="3_LOW")
df$MUTATION_RATE_CATEGORY = factor(df$MUTATION_RATE_CATEGORY, levels = c("1_HIGHEST", "2_HIGH", "3_LOW"))
df[df$TOTAL_SNV_COUNT>228,]$MUTATION_RATE_CATEGORY="2_HIGH"
df[df$TOTAL_SNV_COUNT>2465,]$MUTATION_RATE_CATEGORY="1_HIGHEST"

# Create new SUBTYPE Column
df = transform(df, SUBTYPE="NA")
df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3", "Mixed", "Serous"))
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endo-Grade-1"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endo-Grade-2"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endo-Grade-3"
df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

# Restrict to Sequenced Cases
sub_df = subset(df, SEQUENCED=="Y")

# Sort by TOTAL_SNV_COUNT
sub_df = sub_df[order (sub_df$TOTAL_SNV_COUNT, decreasing=T),]
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

# Create Plot of SNV Rates
qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", 
	xlab="All Sequenced Cases (Ordered by Mutation Count)", ylab="log10(Total # of SNVs)",
	main="Total Mutation Counts")+geom_vline(xintercept = 18, linetype=2)+geom_vline(xintercept=95, linetype=2)+scale_size(to = c(2, 10))+scale_y_log10() 

# Create Plot of SNV Rates, Color-Coded by MUTATION_RATE_CATEGORY
qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MUTATION_RATE_CATEGORY,
			xlab="All Sequenced Cases (Ordered by Mutation Count)", ylab="log10(Total # of SNVs)",
			main="Total Mutation Counts, Color-Coded by MSI-Status")+geom_vline(xintercept = 18, linetype=2)+geom_vline(xintercept=95, linetype=2)+scale_y_log10() 


# Create Plot of SNV Rates, Color-Coded by MSI-Status
	qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MSI_STATUS,
		xlab="All Sequenced Cases (Ordered by Mutation Count)", ylab="log10(Total # of SNVs)",
		main="Total Mutation Counts, Color-Coded by MSI-Status")+geom_vline(xintercept = 18, linetype=2)+geom_vline(xintercept=95, linetype=2)+scale_y_log10() 

# Create Plot of SNV Rates, Color-Coded by MSI-Status, and Node Size Proportional to INDEL_RATIO
qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MSI_STATUS,
	size=INDEL_RATIO,
	xlab="All Sequenced Cases (Ordered by Mutation Count)", ylab="log10(Total # of SNVs)",
	main="Total Mutation Counts, Color-Coded by MSI-Status")+geom_vline(xintercept = 18, linetype=2)+geom_vline(xintercept=95, linetype=2)+scale_size(to = c(2, 10))+scale_y_log10() 

# Compare INDEL_RATIO in MUTATION_RATE_CATEGORY
kt = kruskal.test(INDEL_RATIO ~ factor(MUTATION_RATE_CATEGORY), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), INDEL_RATIO))
p= p + geom_boxplot(outlier.size =0) 
p= p+ geom_jitter(position=position_jitter(w=0.1), size=3, colour="red")
p=p+xlab("Mutation Rate Category") 
p=p+ylab("InDel Ratio") 
the_title = paste("InDel Ratios Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p=p+opts(title=the_title)
p
pw = pairwise.wilcox.test(sub_df$INDEL_RATIO, sub_df$MUTATION_RATE_CATEGORY, p.adj = "bonf")

# Compare MUTATION_RATE in CNA_CLUSTERs
cna_sub = subset(sub_df, CNA_CLUSTER != "NA")
kt = kruskal.test(TOTAL_SNV_COUNT ~ factor(CNA_CLUSTER), data = cna_sub)
p = ggplot(cna_sub,aes(factor(CNA_CLUSTER), TOTAL_SNV_COUNT))+scale_y_log10()
p= p + geom_boxplot(outlier.size =0) 
p= p+ geom_jitter(position=position_jitter(w=0.1), size=3, colour="red")
p=p+xlab("Copy Number Cluster") 
p=p+ylab("log10(Total # SNVs)") 
the_title = paste("Mutation Rates across all CNA Clusters\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p=p+opts(title=the_title)
p

# Compare MUTATION_RATE in Histological Subtypes
kt = kruskal.test(TOTAL_SNV_COUNT ~ factor(SUBTYPE), data = sub_df)
p = ggplot(sub_df,aes(factor(SUBTYPE), TOTAL_SNV_COUNT))+scale_y_log10()
p= p + geom_boxplot(outlier.size =0) 
p= p+ geom_jitter(position=position_jitter(w=0.1), size=3, colour="red")
p=p+xlab("Subtype") 
p=p+ylab("log10(Total # SNVs)") 
the_title = paste("Mutation Rates across all Subtypes\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p=p+opts(title=the_title)
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
qplot(CNA_ALTERED_1, TOTAL_SNV_COUNT, data=sub_df, geom="point",
	xlab="# of Genes Altered by CNA", ylab="log10(Total # of SNVs)",
	main=title) + geom_smooth(method="lm")+scale_y_log10() 

# End PDF 
dev.off() 
