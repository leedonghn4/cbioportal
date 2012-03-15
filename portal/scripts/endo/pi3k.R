#!/usr/bin/Rscript --no-save
library(heatmap.plus)
library ("gplots")
options(warn=-1)

source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/heatmap.R")
source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/oncoprint.R")
source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/compareClasses.R")
source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/initData.R")
source("/Users/ceramie/dev/cbio-cancer-genomics-portal/portal/scripts/endo/mutex.R")

analyze <- function(class1_df, class2_df, class1_name, class2_name) {
  oncoprints_all(class1_df, class1_name)
  oncoprints_all(class2_df, class2_name)
  metrics = c("PTEN_MUTATED_0", "PIK3CA_MUTATED_0", "PIK3R1_MUTATED_0", "PIK3R2_MUTATED_0", 
              "AKT1_MUTATED_0", "AKT2_MUTATED_0", "AKT3_MUTATED_0")
  results = lapply (metrics, compareClasses_0, class1_df, class2_df, class1_name, class2_name)
  results_df = do.call(rbind, results)
  akt_results = compareAktClasses(class1_df, class2_df, class1_name, class2_name)
  results_df = rbind(results_df, akt_results)
  
  metrics = c("PTEN_MUTATED_1", "PIK3CA_MUTATED_1", "PIK3R1_MUTATED_1", "PIK3R2_MUTATED_1", 
              "AKT1_MUTATED_1", "AKT2_MUTATED_1", "AKT3_MUTATED_1",
              "PTEN_MUTATED_3", "PIK3CA_MUTATED_3", "PIK3R1_MUTATED_3", "PIK3R2_MUTATED_3", 
              "AKT1_MUTATED_3", "AKT2_MUTATED_3", "AKT3_MUTATED_3")
  results = lapply (metrics, compareClasses_1_3, class1_df, class2_df, class1_name, class2_name)
  results_df_2 = do.call(rbind, results)
  results_df = rbind(results_df, results_df_2)
  textplot(results_df, hadj=1, show.rownames=F, cex=0.7, halign="left", valign="top")
  title ("Comparison Summary")
  
  metrics = c("PTEN_MUTATED_3", "PIK3CA_MUTATED_3", "PIK3R1_MUTATED_3", "PIK3R2_MUTATED_3", 
              "AKT1_MUTATED_3", "AKT2_MUTATED_3", "AKT3_MUTATED_3")
  results = lapply (metrics, compareClasses_1_3, class1_df, class2_df, class1_name, class2_name)
  results_df_3 = do.call(rbind, results)
  results_df = rbind(results_df, results_df_3)
  
  # Compute Mutual Exclusivity
  mut_ex_class1 = computeMutExAll(class1_df)
  mut_ex_class2 = computeMutExAll(class2_df)
  textplot(mut_ex_class1, hadj=1, show.rownames=F, cex=0.7, halign="left", valign="top")
  title (paste("MutEx Summary", class1_name))
  textplot(mut_ex_class2, hadj=1, show.rownames=F, cex=0.7, halign="left", valign="top")
  title (paste("MutEx Summary", class2_name))
}

# Read in Unified Clinical File
df = initData()

# Only Sequenced Cases
df = subset(df, SEQUENCED=="Y")

#######################################
# Endometriod v. Serous 
endometriod_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
serous_df = subset(df, SUBTYPE %in% c("Serous"))
serous_df = subset(serous_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
#analyze(endometriod_df, serous_df, "Endometriod", "Serous")

#######################################
# Endometriod Grades 1 and 2 v. Grade 3
endometriod_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
grades_1_2_df = subset(endometriod_df, tumor_grade %in% c("Grade 1", "Grade 2"))
grade_3_df = subset(endometriod_df, tumor_grade %in% c("Grade 3"))
analyze(grades_1_2_df, grade_3_df, "Endometriod:  Grades 1,2", "Endometriod:  Grade 3")

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
