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

# Remove the one outlier that has 0 mutations
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

# Create new Rate Columns
sub_df = transform(sub_df, COVERED_BASES = COVERED_BASES/1e+06)
sub_df = transform(sub_df, SILENT_RATE=SILENT_MUTATION_COUNT/COVERED_BASES)
sub_df = transform(sub_df, NON_SILENT_RATE=NON_SILENT_MUTATION_COUNT/COVERED_BASES)
sub_df = transform(sub_df, INDEL_RATE=INDEL_MUTATION_COUNT/COVERED_BASES)

# Sort by NON_SILENT_RATE
sub_df = sub_df[order (sub_df$NON_SILENT_RATE, decreasing=T),]

# Create Plot of NON_SILENT_RATE + SILENT_RATE
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df)
p = p + geom_point(aes(y=NON_SILENT_RATE), colour="blue", size=3) 
p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=3) 
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10()
p

# Create Plot of NON_SILENT_RATE + SILENT_RATE
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, size=INDEL_RATIO)
p = p + geom_point(aes(y=NON_SILENT_RATE))
p = p + scale_size(to = c(3, 15))
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10()
p

# Create Plot of NON_SILENT_RATE + SILENT_RATE
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=MSI_STATUS)
p = p + geom_point(aes(y=NON_SILENT_RATE), size=4)
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10()
p
