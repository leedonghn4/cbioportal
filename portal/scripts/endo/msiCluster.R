#!/usr/bin/Rscript --no-save
library(heatmap.plus)

# Start PDF
pdf("msi_cluster.pdf", width=9, height=7)
# Read in Unified Clinical File
clin_df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Read in MSI File
msi_df = read.delim("~/SugarSync/endo/data/out/msi_out.txt")

# Merge together
df = merge (clin_df, msi_df)

# Remove outliers
# Case with exactly 1 mutation
df = subset(df, TOTAL_SNV_COUNT>1)

# Extract a smaller subset of columns
df = subset(df, select=c(CASE_ID, BAT40, BAT26, BAT25, D17S250, TGFBII, D5S346, 
           D2S123, PentaD, PentaE, MSI_STATUS, MUTATION_RATE_CLUSTER, SEQUENCED))

# Only Include Sequenced Cases;  Exclude cases where MSI is unknown
df = subset(df, SEQUENCED=="Y" & (MSI_STATUS != "Indeterminant" & MSI_STATUS != "Not Done"))

# Create new Color Columns, default to white
# Note the use of stringsAsFactors.
# If this is not set, strings are automatically converted to factors
df = transform(df, COL1="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL2="#FFFFFF", stringsAsFactors=FALSE)

# Suggested Color Mappings at:  http://colorbrewer2.org/
# Create Color Mappings:  MSI_STATUS
df[df$MSI_STATUS=="MSS",]$COL1="#B3CDE3"
df[df$MSI_STATUS=="MSI-L",]$COL1="#8C96C6"
df[df$MSI_STATUS=="MSI-H",]$COL1="#88419D"

# Create Color Mappings:  MUTATION_RATE_CATEGORY
df[df$MUTATION_RATE_CLUSTER=="1_LOW",]$COL2="#B2E2E2"
df[df$MUTATION_RATE_CLUSTER=="2_HIGH",]$COL2="#66C2A4"
df[df$MUTATION_RATE_CLUSTER=="3_HIGHEST",]$COL2="#238B45"

# Get only MSI-L and MSI-H
#df = subset(df, MSI_STATUS=="MSI-H" | MSI_STATUS=="MSI-L")
#df = subset(df, MSI_STATUS=="MSI-H")

# Extract the part of the matrix that we want to cluster
sub_df1 = subset(df, select=BAT40:D2S123)

# Extract the color annotation part of the matrix
sub_df2 = subset(df, select=COL1:COL2)

# Change the column names for the annotations
colnames(sub_df2) = c("MSI", "Mutation Rate Cluster")

# Convert the Data Frames to Matrices
m1 = as.matrix(sub_df1)
m2 = as.matrix(sub_df2)

# Create the color scheme:  blues
#cols = brewer.pal(2, "Blues")
cols = c("#FFFFFF", "#AAAAAA")

# Create the Heatmap
# margins = (bottom, right)
rownames(m1) = rep("", nrow(m1))
rownames(m2) = rep("", nrow(m2))
heatmap.plus(m1, RowSideColors=m2, margins=c(25,5), col=cols, Colv = NA, scale="none")

# Add Two Color Legends
os_labels=c("MSS", "MSI-L", "MSI-H")
color_codes = c("#B3CDE3", "#8C96C6", "#88419D")
legend ("topleft", bty="y", os_labels, fill=color_codes, 
        title="MSI-Status", inset=c(0.2,0.75))

os_labels=c("Low", "High", "Higest")
color_codes = c("#B2E2E2", "#66C2A4", "#238B45")
legend ("topleft", bty="y", os_labels, fill=color_codes, 
        title="Mutation Rate Cluster", inset=c(0.38,0.75))

dev.off()