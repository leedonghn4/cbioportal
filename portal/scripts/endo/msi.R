# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# First, only get cases for which we have definitive MSI results
df_sub <- subset(df, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L" | MSI_STATUS=="MSS")

# Create new MSI_HIGH Column
df_sub = transform(df_sub, MSI_HIGH=0)
df_sub[df_sub$MSI_STATUS=="MSI-H",]$MSI_HIGH=1

# Focus on MUTATION_RATE_CATEGORY:  HIGHEST v. HIGH
t = table(df_sub$MSI_HIGH, df_sub$MUTATION_RATE_CATEGORY, exclude="3_LOW")
prop.table(t, 2)

fisher.test(t)