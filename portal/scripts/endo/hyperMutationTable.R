#!/usr/bin/Rscript --no-save
library(survival)

#####################################################################
# A Series of Statistical Tests to Determine How the HIGHEST Mutation 
# Category is Different from the HIGH MUTATION Category
#####################################################################

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Encode DFS_STATUS_BOOLEAN
# 0 = Disease Free
# 1 = Recurred / Progressed
df = transform (df, DFS_STATUS_BOOLEAN=NA)
df[df$DFS_STATUS=="DiseaseFree",]$DFS_STATUS_BOOLEAN=0
df[df$DFS_STATUS=="Recurred",]$DFS_STATUS_BOOLEAN=1

# Create new InDel Ratio Column
df = transform(df, INDEL_RATIO = INDEL_MUTATION_COUNT/TOTAL_SNV_COUNT)

# Make CNA Clusters into Factors, instead of Ints
df = transform (df, CNA_CLUSTER=as.factor(CNA_CLUSTER))

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

#####################################################################
# Do Cases within the HIGHEST Mutation Category exhibit different levels
# of Micro-sattelite instability?

# First, only get cases for which we have definitive MSI results
local_df <- subset(sub_df, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

# Create new MSI Column
# MSI = 0 means MSS
# MSI = 1 means HSI-H or MSI-LOW
local_df = transform(local_df, MSI=0)
local_df[local_df$MSI_STATUS=="MSI-H",]$MSI=1
local_df[local_df$MSI_STATUS=="MSI-L",]$MSI=1

# Focus on MUTATION_RATE_CATEGORY:  HIGHEST v. HIGH
t = table(local_df$MSI, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test0 = list (METRIC="Microsatellite Instable", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category exhibit different levels
# of MSI-High?

# First, only get cases for which we have definitive MSI results
local_df <- subset(sub_df, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

# Create new MSI-HIGH Column
# MSI_HIGH = 0 means MSS or MSI-LOW
# MSI = 1 means HSI-H
local_df = transform(local_df, MSI_HIGH=0)
local_df[local_df$MSI_STATUS=="MSI-H",]$MSI_HIGH=1

# Focus on MUTATION_RATE_CATEGORY:  HIGHEST v. HIGH
t = table(local_df$MSI_HIGH, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test1 = list (METRIC="MSI-High", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category exhibit different levels
# of MSI-Low?

# First, only get cases for which we have definitive MSI results
local_df <- subset(sub_df, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

# Create new MSI_LOW Column
# MSI_LOW = 0 means MSS or MSI-HIGH
# MSI_LOW = 1 means HSI-LOW
local_df = transform(local_df, MSI_LOW=0)
local_df[local_df$MSI_STATUS=="MSI-L",]$MSI_LOW=1

# Focus on MUTATION_RATE_CATEGORY:  HIGHEST v. HIGH
t = table(local_df$MSI_LOW, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test2 = list (METRIC="MSI-Low", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category tend to belong to a specific CNA cluster?

# First, only get cases for which we have definitive CNA Clusters
local_df = subset(sub_df, CNA_CLUSTER=="1" | CNA_CLUSTER=="2" | CNA_CLUSTER =="3")

# Create new CNA_CLUSTER_1 Column
local_df = transform(local_df, CNA_CLUSTER_1=0)
local_df[local_df$CNA_CLUSTER=="1",]$CNA_CLUSTER_1=1

t = table(local_df$CNA_CLUSTER_1, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test3 = list (METRIC="CNA Cluster 1", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Create new CNA_CLUSTER_2 Column
local_df = transform(local_df, CNA_CLUSTER_2=0)
local_df[local_df$CNA_CLUSTER=="2",]$CNA_CLUSTER_2=1

t = table(local_df$CNA_CLUSTER_2, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test4 = list (METRIC="CNA Cluster 2", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Create new CNA_CLUSTER_3 Column
local_df = transform(local_df, CNA_CLUSTER_3=0)
local_df[local_df$CNA_CLUSTER=="3",]$CNA_CLUSTER_3=1

t = table(local_df$CNA_CLUSTER_3, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test5 = list (METRIC="CNA Cluster 3", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category tend to belong to a specific Histologic Subtype

# First, only get cases for which we have definitive Histological Subtypes
local_df = subset(sub_df, SUBTYPE != "NA")

# Create new ENDO Column
# Endo = 1 Means Endometrioid (Grades 1-3)
# Endo = 0 Means Serous or Mixed
local_df = transform(local_df, ENDO=0)
local_df[local_df$SUBTYPE=="Endo-Grade-1",]$ENDO=1
local_df[local_df$SUBTYPE=="Endo-Grade-2",]$ENDO=1
local_df[local_df$SUBTYPE=="Endo-Grade-3",]$ENDO=1

t = table(local_df$ENDO, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)

# Store these results in master table
test6 = list (METRIC="Endometriod Grades: 1-3", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category tend to have better or worse survival, compared to the HIGH Group?

local_df = subset(sub_df, MUTATION_RATE_CATEGORY=="1_HIGHEST" | MUTATION_RATE_CATEGORY=="2_HIGH")
dfs_surv = Surv (local_df$DFS_MONTHS, local_df$DFS_STATUS_BOOLEAN)
dfs_surv_fit = survfit(dfs_surv ~ local_df$MUTATION_RATE_CATEGORY)
dfs_log_rank = survdiff (dfs_surv ~ local_df$MUTATION_RATE_CATEGORY)

labels=c("1_HIGHEST_MUT", "2_HIGH_MUT")
colors=c("red", "blue", "green")
plot (dfs_surv_fit, col=colors, yscale=100, xlab="Months Disease Free", ylab="% Disease Free", cex.main=1.0, cex.axis=1.0, cex.lab=1.0, font=1)
legend ("topright", bty="n", labels, fill=colors)
p_val <- 1 - pchisq(dfs_log_rank$chisq, length(dfs_log_rank$n) - 1)
legend ("topright", bty="n", paste("Log-rank test p-value: ", signif(p_val, 4)), inset=c(0.0, 0.37))

test7 = list (METRIC="Survival (DFS)", MUT_HIGHEST=0, MUT_HIGH=0, P_VALUE=signif(p_val, digits=4), TEST="Logrank Test")

t = table(local_df$DFS_STATUS_BOOLEAN, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test8 = list (METRIC="Tumor Recurrence", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category tend to have MLH1 Mutation?

local_df = subset(sub_df, MLH1_HYPERMETHYLATED=="1" | MLH1_HYPERMETHYLATED=="0")
t = table(local_df$MLH1_HYPERMETHYLATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test9 = list (METRIC="MLH1 Hypermethylation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

#####################################################################
# Do Cases within the HIGHEST Mutation Category have a different InDel Ratio

local_df = subset(sub_df, MUTATION_RATE_CATEGORY=="1_HIGHEST" | MUTATION_RATE_CATEGORY=="2_HIGH")
wt = wilcox.test(local_df$INDEL_RATIO ~ factor(local_df$MUTATION_RATE_CATEGORY))
tt = t.test(local_df$INDEL_RATIO ~ factor(local_df$MUTATION_RATE_CATEGORY))
test10 = list (METRIC="InDel Ratio", MUT_HIGHEST=tt$estimate[[1]], MUT_HIGH=tt$estimate[[2]], P_VALUE=signif(tt$p.value, digits=4), TEST="Wilcoxon Test")

#####################################################################
# Do Cases within the HIGHEST Mutation Category have more genes altered by CNA?

local_df = subset(sub_df, MUTATION_RATE_CATEGORY=="1_HIGHEST" | MUTATION_RATE_CATEGORY=="2_HIGH")
local_df = subset(local_df, GISTIC=="Y")
wt = wilcox.test(local_df$CNA_ALTERED_1 ~ factor(local_df$MUTATION_RATE_CATEGORY))
tt = t.test(local_df$CNA_ALTERED_1 ~ factor(local_df$MUTATION_RATE_CATEGORY))
test11 = list (METRIC="Mean # of Genes Altered by CNA (non-diploid)", MUT_HIGHEST=tt$estimate[[1]], MUT_HIGH=tt$estimate[[2]], P_VALUE=signif(tt$p.value, digits=4), TEST="Wilcoxon Test")

wt = wilcox.test(local_df$CNA_ALTERED_2 ~ factor(local_df$MUTATION_RATE_CATEGORY))
tt = t.test(local_df$CNA_ALTERED_2 ~ factor(local_df$MUTATION_RATE_CATEGORY))
test12 = list (METRIC="Mean # of Genes Altered by CNA (amp/del only)", MUT_HIGHEST=tt$estimate[[1]], MUT_HIGH=tt$estimate[[2]], P_VALUE=signif(tt$p.value, digits=4), TEST="Wilcoxon Test")

#####################################################################
# Do Cases within the HIGHEST Mutation Category have differente rates of specific gene mutations?

local_df = subset(sub_df, SEQUENCED=="Y")
t = table(local_df$PTEN_MUTATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test13 = list (METRIC="Rate of PTEN Mutation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

t = table(local_df$TP53_MUTATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test14 = list (METRIC="Rate of TP53 Mutation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

t = table(local_df$PIK3CA_MUTATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test15 = list (METRIC="Rate of PIK3CA Mutation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

t = table(local_df$MLH1_MUTATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test16 = list (METRIC="Rate of MLH1 Mutation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

t = table(local_df$KRAS_MUTATED, local_df$MUTATION_RATE_CATEGORY, exclude="3_LOW")
pt = prop.table(t, 2)
f = fisher.test(t)
test17 = list (METRIC="Rate of KRAS Mutation", MUT_HIGHEST=pt[2,1], MUT_HIGH=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

options(scipen=11)
results = rbind (data.frame(test0), data.frame(test1), data.frame(test2), data.frame(test3), data.frame(test4), data.frame(test5), data.frame(test6), data.frame(test7),
	data.frame(test8), data.frame(test9), data.frame(test10), data.frame(test11), data.frame(test12), data.frame(test13), data.frame(test14), data.frame(test15),
	data.frame(test16), data.frame(test17))
print(results)
write.table(results, file="table.txt", row.names=FALSE, sep="\t", quote = FALSE)

