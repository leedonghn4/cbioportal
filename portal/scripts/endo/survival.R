#!/usr/bin/Rscript --no-save
# Load ggplots2
library(ggplot2)
library(survival)

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Encode MUTATION_RATE_NEW
# 0 = Disease Free
# 1 = Recurred / Progressed
df = transform (df, MUTATION_RATE_NEW=NA)
df[df$MUTATION_RATE_CATEGORY=="1_HIGHEST",]$MUTATION_RATE_NEW="HIGHEST"
df[df$MUTATION_RATE_CATEGORY=="2_HIGH",]$MUTATION_RATE_NEW="HIGH_AND_LOW"
df[df$MUTATION_RATE_CATEGORY=="3_LOW",]$MUTATION_RATE_NEW="HIGH_AND_LOW"

# Encode DFS_STATUS_BOOLEAN
# 0 = Disease Free
# 1 = Recurred / Progressed
df = transform (df, DFS_STATUS_BOOLEAN=NA)
df[df$DFS_STATUS=="DiseaseFree",]$DFS_STATUS_BOOLEAN=0
df[df$DFS_STATUS=="Recurred",]$DFS_STATUS_BOOLEAN=1

# Restrict to Sequenced Cases
sub_df = subset(df, SEQUENCED=="Y")

dfs_surv = Surv (sub_df$DFS_MONTHS, sub_df$DFS_STATUS_BOOLEAN)

dfs_surv_fit = survfit(dfs_surv ~ sub_df$MUTATION_RATE_CATEGORY)
dfs_log_rank = survdiff (dfs_surv ~ sub_df$MUTATION_RATE_CATEGORY)
labels=c("1_HIGHEST_MUT", "2_HIGH_MUT", "3_LOW")
colors=c("red", "blue", "green")
plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
legend ("topright", bty="n", labels, fill=colors)
p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))

dfs_surv_fit = survfit(dfs_surv ~ sub_df$MUTATION_RATE_NEW)
dfs_log_rank = survdiff (dfs_surv ~ sub_df$MUTATION_RATE_NEW)
labels=c("HIGH_AND_LOW", "HIGHEST")
colors=c("red", "blue")
plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
legend ("topright", bty="n", labels, fill=colors)
p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))


dfs_surv_fit = survfit(dfs_surv ~ sub_df$CNA_CLUSTER)
dfs_log_rank = survdiff (dfs_surv ~ sub_df$CNA_CLUSTER)
labels=c("CNA_CLUSTER_1", "CNA_CLUSTER_2", "CNA_CLUSTER_3")
colors=c("red", "blue", "green")
plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
legend ("topright", bty="n", labels, fill=colors)
p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))
