#!/usr/bin/Rscript --no-save
library("pdfCluster")
library(ggplot2)

#####################################################################
# A Series of Graphs Exploring Hypermutation in Endometrical Cancer
#####################################################################

# Start PDF
pdf("report.pdf", width=9, height=7) 

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Make CNA Clusters into Factors, instead of Ints
df = transform (df, CNA_CLUSTER=as.factor(CNA_CLUSTER))

# Make MLH1 Hypermethylation into Factors, instead of Ints
df = transform (df, MLH1_HYPERMETHYLATED=as.factor(MLH1_HYPERMETHYLATED))

# Create new InDel Ratio Column
df = transform(df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

# Restrict to Sequenced Cases
sub_df = subset(df, SEQUENCED=="Y")

# Removeoutliers:
# Case with 0 mutations
# Case with > 20,000 mutations
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)
#sub_df = subset(sub_df, TOTAL_SNV_COUNT<20000)

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

plot(density(log10(sub_df$SILENT_RATE)))

# Perform Density Estimate Clustering on NON_SILENT_RATE
cl <- pdfCluster(log10(sub_df$NON_SILENT_RATE), n.stage=55, hmult=0.5)
# append cluster assignment
sub_df <- data.frame(sub_df, cl@clusters)
plot(cl)

# Create Plot of NON_SILENT_RATE + SILENT_RATE
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df)
p = p + geom_point(aes(y=NON_SILENT_RATE), colour="blue", size=3) 
p = p + geom_point(aes(y=SILENT_RATE), colour="red", size=3) 
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(scientific=FALSE)
p

# Create Plot of NON_SILENT_RATE, size=INDEL_RATIO
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, size=INDEL_RATIO, shape=1)
p = p + geom_point(aes(y=NON_SILENT_RATE))
p = p + scale_size(to = c(1, 8))
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10()
p

# Create Plot of NON_SILENT_RATE, color=MSI
t0 <- floor(log10(range(sub_df$NON_SILENT_RATE))) 
t1 <- seq(from=t0[1], to=t0[2]) 
p = qplot(1:nrow(sub_df), NON_SILENT_RATE, data=sub_df, colour=MSI_STATUS)
p = p + geom_point(aes(y=NON_SILENT_RATE), size=3)
p = p + opts(title="Mutation Rates")
p = p + ylab("Mutation Rate (mutations per 10^6 bases)")
p = p + xlab("All Sequenced Cases (Ordered by NonSilent Mutation Rate)")
p = p + scale_y_log10(breaks=10^t1, labels=format(10^t1, big.mark=',', scientific=FALSE, trim=TRUE, drop0trailing=T))
p

# Get Summary Stats
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
print(summary)