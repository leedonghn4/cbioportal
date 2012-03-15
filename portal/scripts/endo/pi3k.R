#!/usr/bin/Rscript --no-save
library(heatmap.plus)
library ("gplots")
options(warn=-1)

source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/heatmap.R")

compareClasses_0 <- function(metric, class1_df, class2_df, class1, class2) {
  num_class1_altered_no = nrow(class1_df[class1_df[[metric]]==0,])
  num_class1_altered_yes = nrow(class1_df[class1_df[[metric]]==1,])
  
  num_class2_altered2_no = nrow(class2_df[class2_df[[metric]]==0,])
  num_class2_altered_yes = nrow(class2_df[class2_df[[metric]]==1,])
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered2_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC=metric, class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}

compareClasses_1_3 <- function(metric, class1_df, class2_df, class1, class2) {
  num_class1_altered_no = nrow(class1_df[class1_df[[metric]]==0,])
  num_class1_altered_yes = nrow(class1_df[class1_df[[metric]]==2,])
  
  num_class2_altered2_no = nrow(class2_df[class2_df[[metric]]==0,])
  num_class2_altered_yes = nrow(class2_df[class2_df[[metric]]==2,])
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered2_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC=metric, class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}

compareAktClasses <- function(class1_df, class2_df, class1, class2) {
  num_class1_altered_yes = nrow(subset(class1_df, AKT1_MUTATED_0==1 | AKT2_MUTATED_0==1 | AKT3_MUTATED_0==1))
  num_class1_altered_no = nrow(class1_df) - num_class1_altered_yes
  
  num_class2_altered_yes = nrow(subset(class2_df, AKT1_MUTATED_0==1 | AKT2_MUTATED_0==1 | AKT3_MUTATED_0==1))
  num_class2_altered_no = nrow(class2_df) - num_class2_altered_yes
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC="AKT123_MUTATED_0", class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}

oncoprint <- function (m1, m2, colors, title) {
  colnames(m1) = rep("", ncol(m1))
  colnames(m2) = c("MSI", "Mutation Rate", "PTEN - Protein", "AKT_pS473", "AKT_pT308")
  heatmap.plus2(m1, margins=c(5,15), cexRow=1.0, scale="none", col=colors, ColSideColors=m2)
}

oncoprints_all <- function(sub_df, title) {
  textplot (title, col="darkblue", valign="top", cex=2.0)
  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_0, PIK3CA_MUTATED_0, PIK3R1_MUTATED_0, 
      PIK3R2_MUTATED_0, AKT1_MUTATED_0, AKT2_MUTATED_0, AKT3_MUTATED_0))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 0"))

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_1, PIK3CA_MUTATED_1, PIK3R1_MUTATED_1, 
      PIK3R2_MUTATED_1, AKT1_MUTATED_1, AKT2_MUTATED_1, AKT3_MUTATED_1))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "pink", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 1")) 

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_3, PIK3CA_MUTATED_3, PIK3R1_MUTATED_3, 
      PIK3R2_MUTATED_3, AKT1_MUTATED_3, AKT2_MUTATED_3, AKT3_MUTATED_3))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
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

# Create Color Mappings
df = transform(df, COL1="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL2="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL3="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL4="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL5="#FFFFFF", stringsAsFactors=FALSE)
msi_colors = c("#DDDDDD", "#CCFFCC", "#FFCCFF", "#FF0000")
cna_colors = c("#CCFFFF", "#33FFFF", "#0000FF")
rppa_colors = c("red", "green")
df[df$MSI_STATUS %in% "Indeterminant",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "Not Done",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "MSS",]$COL1=msi_colors[2]
df[df$MSI_STATUS %in% "MSI-L",]$COL1=msi_colors[3]
df[df$MSI_STATUS %in% "MSI-H",]$COL1=msi_colors[4]
df[df$MUTATION_RATE_CLUSTER %in% "1_LOW",]$COL2=cna_colors[1]
df[df$MUTATION_RATE_CLUSTER %in% "2_HIGH",]$COL2=cna_colors[2]
df[df$MUTATION_RATE_CLUSTER %in% "3_HIGHEST",]$COL2=cna_colors[3]

df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$PTEN_PROTEIN_LEVEL <= 0,]$COL3=rppa_colors[1]
df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$PTEN_PROTEIN_LEVEL > 0,]$COL3=rppa_colors[2]

df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$AKT_pS473 <= 0,]$COL4=rppa_colors[1]
df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$AKT_pS473 > 0,]$COL4=rppa_colors[2]

df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$AKT_pT308 <= 0,]$COL5=rppa_colors[1]
df[!is.na(df$PTEN_PROTEIN_LEVEL) & df$AKT_pT308 > 0,]$COL5=rppa_colors[2]

plot(density(df$PTEN_PROTEIN_LEVEL, na.rm=T))
hist(df$PTEN_PROTEIN_LEVEL, na.rm=T)

plot(density(df$AKT_pS473, na.rm=T))
hist(df$AKT_pS473, na.rm=T)

plot(density(df$AKT_pT308, na.rm=T))
hist(df$AKT_pT308, na.rm=T)

# Only Sequenced Cases
df = subset(df, SEQUENCED=="Y")

#######################################
# Endometriod Only
endometriod_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
oncoprints_all(endometriod_df, "Endometriod")

#######################################
# Serous Only
serous_df = subset(df, SUBTYPE %in% c("Serous"))
serous_df = subset(serous_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
oncoprints_all(serous_df, "Serous")

# Compare the two classes
metrics = c("PTEN_MUTATED_0", "PIK3CA_MUTATED_0", "PIK3R1_MUTATED_0", "PIK3R2_MUTATED_0", 
            "AKT1_MUTATED_0", "AKT2_MUTATED_0", "AKT3_MUTATED_0")
results = lapply (metrics, compareClasses_0, endometriod_df, serous_df, "ENDOMETRIOD", "SEROUS")
results_df = do.call(rbind, results)
akt_results = compareAktClasses(endometriod_df, serous_df, "ENDOMETRIOD", "SEROUS")
results_df = rbind(results_df, akt_results)

metrics = c("PTEN_MUTATED_1", "PIK3CA_MUTATED_1", "PIK3R1_MUTATED_1", "PIK3R2_MUTATED_1", 
            "AKT1_MUTATED_1", "AKT2_MUTATED_1", "AKT3_MUTATED_1")
results = lapply (metrics, compareClasses_1_3, endometriod_df, serous_df, "ENDOMETRIOD", "SEROUS")
results_df_2 = do.call(rbind, results)
results_df = rbind(results_df, results_df_2)

metrics = c("PTEN_MUTATED_3", "PIK3CA_MUTATED_3", "PIK3R1_MUTATED_3", "PIK3R2_MUTATED_3", 
            "AKT1_MUTATED_3", "AKT2_MUTATED_3", "AKT3_MUTATED_3")
results = lapply (metrics, compareClasses_1_3, endometriod_df, serous_df, "ENDOMETRIOD", "SEROUS")
results_df_3 = do.call(rbind, results)
results_df = rbind(results_df, results_df_3)

#######################################
# Endometriod:  MUT LOW
#sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
#sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
#oncoprints_all(sub_df, "Endometriod - Mut Low")

#######################################
# Endometriod:  MUT HIGH
#sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
#sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "2_HIGH")
#oncoprints_all(sub_df, "Endometriod - Mut High")

#######################################
# Endometriod:  MUT HIGHEST
#sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
#sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "3_HIGHEST")
#oncoprints_all(sub_df, "Endometriod - Mut Highest")
