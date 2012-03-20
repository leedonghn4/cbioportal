# Reads in and Inits Data

initData <- function() {
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
  df = transform(df, COL6="#FFFFFF", stringsAsFactors=FALSE)
  msi_colors = c("#DDDDDD", "#CCFFCC", "#FFCCFF", "#FF0000")
  cna_colors = c("#CCFFFF", "#33FFFF", "#0000FF")
  rppa_colors = c("red", "green")
  df[df$MSI_STATUS %in% "Indeterminate",]$COL1=msi_colors[1]
  df[df$MSI_STATUS %in% "Not Tested",]$COL1=msi_colors[1]
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

  df[df$tumor_grade %in% "Grade 1",]$COL6=cna_colors[1]
  df[df$tumor_grade %in% "Grade 2",]$COL6=cna_colors[2]
  df[df$tumor_grade %in% "Grade 3",]$COL6=cna_colors[3]
  return (df)
}