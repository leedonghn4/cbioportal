#!/usr/bin/Rscript --no-save

library(ggplot2)

plotMeth <- function (gene, merged_df) {
  tryCatch({
    plot(density(merged_df[[gene]], na.rm=TRUE), main=gene)
#     p = qplot(1:nrow(merged_df), merged_df[[gene]], colour=merged_df$MUTATION_RATE_CLUSTER, data=merged_df)
#     p = p + labs(colour = "Mut_Clusters")
#     p = p + geom_point(size = 4)
#     print(p)

    kt = kruskal.test(merged_df[[gene]] ~ factor(merged_df$MUTATION_RATE_CLUSTER), data = merged_df)
    yrange = range (merged_df[[gene]], na.rm = TRUE)
    boxplot(merged_df[[gene]] ~ merged_df$MUTATION_RATE_CLUSTER, outline=F, ylim = yrange)
    stripchart (merged_df[[gene]] ~ merged_df$MUTATION_RATE_CLUSTER, vertical=T, add=T, pch=20, col=c("red", "blue", "green"), method="jitter", cex=2, jitter=.10)
    title = paste (gene, ", " , signif(kt$p.value, 4))
    title (main=title, xlab="Mutation Clusters", ylab="Hypermethylation")
    if (kt$p.value < 0.05) {
      cat ("** Significant Difference for gene:  ", gene, ", p-value:  ", signif(kt$p.value, 4), "\n")
    }
  }, error = function(ex) {
    #cat("An error was detected.  Data was probably missing for gene:  ", gene, ".\n");
    #print(ex);
  })
}

# Read in Methylation Data
df = read.delim("~/SugarSync/endo/data/dna_repair_meth.txt")

clin_df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

merged_df = merge(df, clin_df)

genes = names(df)
genes = genes[2:length(genes)]
results = lapply (genes, plotMeth, merged_df)