#!/usr/bin/Rscript --no-save
# Load ggplots2
library("pdfCluster")
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

# Restrict to Endomterial Subtypes
#sub_df = subset(sub_df, SUBTYPE=="Endo-Grade-1" | SUBTYPE=="Endo-Grade-2" | SUBTYPE=="Endo-Grade-3")

# Remove the two outliers:
# Case with 0 mutations
# Case with > 20,000 mutations
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)
sub_df = subset(sub_df, TOTAL_SNV_COUNT<20000)

# Create new Rate Columns
sub_df = transform(sub_df, COVERED_BASES = COVERED_BASES/1e+06)
sub_df = transform(sub_df, SILENT_RATE=SILENT_MUTATION_COUNT/COVERED_BASES)
sub_df = transform(sub_df, NON_SILENT_RATE=NON_SILENT_MUTATION_COUNT/COVERED_BASES)
sub_df = transform(sub_df, TOTAL_SNV_RATE=TOTAL_SNV_COUNT/COVERED_BASES)
sub_df = transform(sub_df, INDEL_RATE=INDEL_MUTATION_COUNT/COVERED_BASES)

# Sort by NON_SILENT_RATE
sub_df = sub_df[order (sub_df$NON_SILENT_RATE, decreasing=T),]

# Plot Density Estimate of NON_SILENT RATE
plot(density(log10(sub_df$NON_SILENT_RATE)))
abline(v=2.0,col="red",lty=3)
abline(v=.65, col="red", lty=3)

# Perform Density Estimate Clustering on NON_SILENT_RATE
cl <- pdfCluster(log10(sub_df$NON_SILENT_RATE), n.stage=25, hmult=0.7)
# append cluster assignment
sub_df <- data.frame(sub_df, cl@clusters)
plot(cl)


# Create Plot of NON_SILENT_RATE + SILENT_RATE + INDEL_RATE
p = qplot(1:nrow(sub_df), TOTAL_SNV_RATE, data=sub_df)
p = p + geom_point(aes(y=TOTAL_SNV_RATE), colour="blue", size=3) 
p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=3) 
p = p + geom_point(aes(y=INDEL_RATE), colour="green", size=3)
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(scientific=FALSE)
p

# Perform K-Means Clustering, with N clusters
#local_df = subset(sub_df, select=c("SILENT_RATE", "NON_SILENT_RATE", "INDEL_RATE"))
local_df = subset(sub_df, select=c("NON_SILENT_RATE"))
fit <- kmeans(local_df, 4)
# get cluster means 
aggregate(local_df,by=list(fit$cluster),FUN=mean)
# append cluster assignment
sub_df <- data.frame(sub_df, fit$cluster)
sub_df <- transform(sub_df, fit.cluster = as.factor(fit.cluster))

# Create Plot of NON_SILENT_RATE, Color-Code by KMeans Clustering
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=fit.cluster, shape=MSI_STATUS)
p = p + geom_point(aes(y=NON_SILENT_RATE), size=3) 
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10()
p

# Create Plot of NON_SILENT_RATE + SILENT_RATE, size=INDEL_RATIO
# p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, size=INDEL_RATIO)
# p = p + geom_point(aes(y=NON_SILENT_RATE))
# p = p + scale_size(to = c(3, 15))
# p = p + opts(title="Mutation Rates")
# p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
# p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
# p = p + scale_y_log10()
# p

# Create Plot of NON_SILENT_RATE + SILENT_RATE, color=MSI
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=MSI_STATUS)
p = p + geom_point(aes(y=NON_SILENT_RATE), size=1)
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p

t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=SUBTYPE)
p = p + geom_point(aes(y=NON_SILENT_RATE), size=1)
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p

local_df = subset(sub_df, select=c("SILENT_RATE", "NON_SILENT_RATE", "INDEL_RATE"))
local_df = transform (local_df, SILENT_RATE = log10(SILENT_RATE))
local_df = transform (local_df, NON_SILENT_RATE = log10(NON_SILENT_RATE))
local_df = transform (local_df, INDEL_RATE = log10(INDEL_RATE)+0.0001)

cl <- pdfCluster(local_df, n.stage=25, hmult=0.7)
# append cluster assignment
plot(cl)