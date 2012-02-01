#!/usr/bin/Rscript --no-save

print("Generating Clinical Plots...  Loading Packages...")

sink(file="/dev/null")
suppressMessages(library (ggplot2))
suppressMessages(library(gridExtra))
sink()

# Start PDF
pdf("clinical.pdf", width=9, height=7)

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

df = transform(df, MUTATION_RATE_CLUSTER=as.factor(MUTATION_RATE_CLUSTER))

# Create new SUBTYPE Column
df = transform(df, SUBTYPE="NA")
df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endo-G1", "Endo-G2", "Endo-G3", "Mixed", "Serous"))
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endo-G1"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endo-G2"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endo-G3"
df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

# Various Charts of Clinical Factors
df_sub = subset(df, SUBTYPE != "NA")

# Pie Chart of Sub Types
p1 = ggplot(df_sub, aes(x = factor(1), fill = factor(SUBTYPE))) + geom_bar(width = 1)
p1 = p1 + coord_polar(theta = "y")
p1 = p1 + opts(title = "Histological Subtype")
p1 = p1 + opts(axis.text.y = theme_blank(), axis.ticks = theme_blank())+xlab("") + ylab("")

# Break out Histological subtypes by Grade
p2 = ggplot(df_sub, aes(x = factor(1), fill = factor(SUBTYPE))) + geom_bar(width = 1)
p2 = p2 + opts(title = "Histological Subtype and Grade") 
p2 = p2 + facet_grid(facets=. ~ tumor_grade)
p2 = p2 + xlab("") + opts(axis.text.x = theme_blank(), axis.ticks = theme_blank())

# Survival Status
p3 = ggplot(df_sub, aes(x = factor(1), fill = factor(OS_STATUS))) + geom_bar(width = 1)
p3 = p3 + coord_polar(theta = "y")
p3 = p3 + opts(title = "Survival:  Living")
p3 = p3 + opts(axis.text.y = theme_blank(), axis.ticks = theme_blank())+xlab("") + ylab("")

# Survival Status, Break out by Histological Subtypes
df_sub = subset(df, SUBTYPE != "NA" & DFS_STATUS != "")
p4 = ggplot(df_sub, aes(x = factor(1), fill = factor(DFS_STATUS))) + geom_bar(width = 1)
p4 = p4 + coord_polar(theta = "y")
p4 = p4 + opts(title = "Survival:  Recurrence")
p4 = p4 + opts(axis.text.y = theme_blank(), axis.ticks = theme_blank())+xlab("") + ylab("")

grid.arrange(p1, p2, p3, p4, nrow=2)

# What fraction of patients exhibit micro-sattellite instability?
p1 = ggplot(df, aes(x = factor(1), fill = factor(MSI_STATUS))) + geom_bar(width = 1)
p1 = p1 + coord_polar(theta = "y")
p1 = p1 + opts(title = "MSI Status")
p1 = p1 + opts(axis.text.y = theme_blank(), axis.ticks = theme_blank())+xlab("") + ylab("")

# Break out MSI by histological subtypes
df_sub = subset(df, SUBTYPE != "NA")
p2 = ggplot(df_sub, aes(x = factor(1), fill = factor(MSI_STATUS))) + geom_bar(width = 1)
p2 = p2 + opts(title = "MSI Status:  By Subtypes") 
p2 = p2 + facet_grid(facets=. ~ SUBTYPE)
p2 = p2 + xlab("") + opts(axis.text.x = theme_blank(), axis.ticks = theme_blank())

grid.arrange(p1, p2, nrow=2)

garbage = dev.off()

print ("PDF report written to:  clinical.pdf")
