# Code for Generating Pseudo-Oncoprints

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
