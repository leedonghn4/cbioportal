#!/usr/bin/Rscript --no-save
# Load ggplots2
library(ggplot2)

#####################################################################
# A Series of Graphs Exploring Hypermutation in Endometrical Cancer
#####################################################################

# Start PDF
#pdf("report.pdf", width=9, height=7) 

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Make CNA Clusters into Factors, instead of Ints
df = transform (df, CNA_CLUSTER=as.factor(CNA_CLUSTER))

# Make MLH1 Hypermethylation into Factors, instead of Ints
df = transform (df, MLH1_HYPERMETHYLATED=as.factor(MLH1_HYPERMETHYLATED))

# Create new InDel Ratio Column
df = transform(df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

# Create new SUBTYPE Column that has Shorter Labels
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

# Remove the one outlier that has 0 mutations
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

# Create Plot of SNV Rates, Color Coded by Mutation Categories, Log Scale
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MUTATION_RATE_CATEGORY)
p = p + opts(title="Total Mutation Counts")
p = p + scale_y_continuous("log10(Total # of SNVs)")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p = p + scale_y_log10()
p

# Create Plot of SNV Rates, Color Coded by Mutation Categories, Plain Scale
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MUTATION_RATE_CATEGORY)
p = p + opts(title="Total Mutation Counts")
p = p + scale_y_continuous("Total # of SNVs")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p

# Create Plot of SNV Rates, Color-Coded by MSI-Status, Node Size is Proportional to INDEL_RATIO
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MSI_STATUS, size=INDEL_RATIO)
p = p + opts(title="Total Mutation Counts")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p = p + ylab("log(Total # of SNVs)")
p = p + scale_size(to = c(2, 10))
p = p + scale_y_log10() 
p = p + scale_colour_brewer(type="qual", palette=6)
p

# Create Plot of SNV Rates, Color-Coded by MLH1 Hypermethylation
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=MLH1_HYPERMETHYLATED)
p = p + opts(title="Total Mutation Counts")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p = p + ylab("log(Total # of SNVs)")
p = p + scale_size(to = c(2, 10))
p = p + scale_y_log10() 
p = p + scale_colour_brewer(type="qual", palette=6) 
p = p + geom_rug(aes(y = NULL)) 
p

# Create Plot of SNV Rates, Color-Coded by MLH1 Mutation
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", colour=factor(MLH1_MUTATED))
p = p + opts(title="Total Mutation Counts")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p = p + ylab("log(Total # of SNVs)")
p = p + scale_size(to = c(2, 10))
p = p + scale_y_log10() 
p = p + geom_rug(aes(y = NULL)) 
p

# Create Plot of SNV Rates, Node-Size is Proportional to CNA_ALTERED_1
p = qplot(1:nrow(sub_df), TOTAL_SNV_COUNT, data=sub_df, geom="point", size=CNA_ALTERED_1)
p = p + opts(title="Total Mutation Counts")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p = p + ylab("log(Total # of SNVs)")
p = p + scale_size(to = c(2, 10))
p = p + scale_y_log10() 
p

# Compare INDEL_RATIO in MUTATION_RATE_CATEGORY
kt = kruskal.test(INDEL_RATIO ~ factor(MUTATION_RATE_CATEGORY), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), INDEL_RATIO))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("InDel Ratio") 
the_title = paste("InDel Ratios Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p
pw = pairwise.wilcox.test(sub_df$INDEL_RATIO, sub_df$MUTATION_RATE_CATEGORY, p.adj = "bonf")

# Compare MUTATION_RATE in CNA_CLUSTERs
cna_sub = subset(sub_df, CNA_CLUSTER != "NA")
kt = kruskal.test(TOTAL_SNV_COUNT ~ factor(CNA_CLUSTER), data = cna_sub)
p = ggplot(cna_sub,aes(factor(CNA_CLUSTER), TOTAL_SNV_COUNT))+scale_y_log10()
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3, aes(colour=MUTATION_RATE_CATEGORY))
p = p + xlab("Copy Number Cluster") 
p = p + ylab("log10(Total # SNVs)") 
the_title = paste("Mutation Rates across all CNA Clusters\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p

# Compare MUTATION_RATE in Histological Subtypes
kt = kruskal.test(TOTAL_SNV_COUNT ~ factor(SUBTYPE), data = sub_df)
p = ggplot(sub_df,aes(factor(SUBTYPE), TOTAL_SNV_COUNT))+scale_y_log10()
p = p + geom_boxplot(outlier.size =0)
p = p + geom_jitter(position=position_jitter(w=0.1), size=3, aes(colour=MUTATION_RATE_CATEGORY))
p = p + xlab("Subtype") 
p = p + ylab("log10(Total # SNVs)") 
the_title = paste("Mutation Rates across all Subtypes\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p= p + opts(title=the_title)
p

# Compare AGE in MUTATION_RATE_CATEGORY
kt = kruskal.test(age_at_initial_pathologic_diagnosis ~ factor(MUTATION_RATE_CATEGORY), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), age_at_initial_pathologic_diagnosis))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Age at Diagnosis") 
the_title = paste("Age Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p

# Compare AGE in CNA_CLUSTER
kt = kruskal.test(age_at_initial_pathologic_diagnosis ~ factor(CNA_CLUSTER), data = sub_df)
p = ggplot(sub_df,aes(factor(CNA_CLUSTER), age_at_initial_pathologic_diagnosis))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Age at Diagnosis") 
the_title = paste("Age Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
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
