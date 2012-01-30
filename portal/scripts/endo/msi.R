# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Filter:
# 1. only sequenced cases
# 2. only cases that have a MUTATION_RATE_CLUSTER
# 2. only get cases for which we have definitive MSI results
df_sub = subset(df, SEQUENCED=="Y")
df_sub = subset (df_sub, MUTATION_RATE_CLUSTER != "NA")
df_sub <- subset(df_sub, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

t = table(df_sub$MSI_STATUS, df_sub$MUTATION_RATE_CLUSTER, exclude=c("Indeterminant", "Not Done"))
prop.table(t, 2)

fisher.test(t)