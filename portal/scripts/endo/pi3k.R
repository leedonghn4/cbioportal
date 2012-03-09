#!/usr/bin/Rscript --no-save
library(heatmap.plus)
library ("gplots")
options(warn=-1)

oncoprint <- function (m1, m2, colors, title) {
  colnames(m1) = rep("", ncol(m1))
  colnames(m2) = c("MSI", "Mutation Rate")
  heatmap.plus(m1, margins=c(5,15), cexRow=1.0, scale="none", col=colors, ColSideColors=m2)
}

oncoprints_all <- function(sub_df, title) {
  textplot (title, col="darkblue", valign="top", cex=2.0)
  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_0, PIK3CA_MUTATED_0, PIK3R1_MUTATED_0, 
      PIK3R2_MUTATED_0, AKT1_MUTATED_0, AKT2_MUTATED_0, AKT3_MUTATED_0))
  colors = subset(sub_df, select=c(COL1, COL2))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 0"))

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_1, PIK3CA_MUTATED_1, PIK3R1_MUTATED_1, 
      PIK3R2_MUTATED_1, AKT1_MUTATED_1, AKT2_MUTATED_1, AKT3_MUTATED_1))
  colors = subset(sub_df, select=c(COL1, COL2))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "pink", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 1")) 

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_3, PIK3CA_MUTATED_3, PIK3R1_MUTATED_3, 
      PIK3R2_MUTATED_3, AKT1_MUTATED_3, AKT2_MUTATED_3, AKT3_MUTATED_3))
  colors = subset(sub_df, select=c(COL1, COL2))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "pink", "red")
  oncoprint(m1, m2, colors, paste(title, "Mut - Level 3")) 
}  


# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Create new SUBTYPE Column that has Shorter Labels
df = transform(df, SUBTYPE="NA")
df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3", "Mixed", "Serous"))
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endo-Grade-1"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endo-Grade-2"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endo-Grade-3"
df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

# Create Color Mappings:  MSI_STATUS and CNA_CLUSTER
df = transform(df, COL1="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL2="#FFFFFF", stringsAsFactors=FALSE)
msi_colors = c("#DDDDDD", "#CCFFCC", "#FFCCFF", "#FF0000")
cna_colors = c("#CCFFFF", "#33FFFF", "#0000FF")
df[df$MSI_STATUS %in% "Indeterminant",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "Not Done",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "MSS",]$COL1=msi_colors[2]
df[df$MSI_STATUS %in% "MSI-L",]$COL1=msi_colors[3]
df[df$MSI_STATUS %in% "MSI-H",]$COL1=msi_colors[4]
df[df$MUTATION_RATE_CLUSTER %in% "1_LOW",]$COL2=cna_colors[1]
df[df$MUTATION_RATE_CLUSTER %in% "2_HIGH",]$COL2=cna_colors[2]
df[df$MUTATION_RATE_CLUSTER %in%"3_HIGHEST",]$COL2=cna_colors[3]

# Only Sequenced Cases
df = subset(df, SEQUENCED=="Y")

#######################################
# Endometriod Only
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
oncoprints_all(sub_df, "Endometriod")

#######################################
# Serous Only
sub_df = subset(df, SUBTYPE %in% c("Serous"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
oncoprints_all(sub_df, "Serous")

#######################################
# Endometriod:  MUT LOW
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
oncoprints_all(sub_df, "Endometriod - Mut Low")

#######################################
# Endometriod:  MUT HIGH
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "2_HIGH")
oncoprints_all(sub_df, "Endometriod - Mut High")

#######################################
# Endometriod:  MUT HIGHEST
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "3_HIGHEST")
oncoprints_all(sub_df, "Endometriod - Mut Highest")
