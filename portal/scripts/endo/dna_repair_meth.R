#!/usr/bin/Rscript --no-save

library(ggplot2)

# Compare Methylation of Specified Gene Across the Three Mutation Rate Clusters
compareThreeWay <- function (gene, merged_df) {
  tryCatch({
    plot(density(merged_df[[gene]], na.rm=TRUE), main=gene)
    kt = kruskal.test(merged_df[[gene]] ~ factor(merged_df$MUTATION_RATE_CLUSTER), data = merged_df)
    yrange = range (merged_df[[gene]], na.rm = TRUE)
    boxplot(merged_df[[gene]] ~ merged_df$MUTATION_RATE_CLUSTER, outline=F, ylim = yrange)
    stripchart (merged_df[[gene]] ~ merged_df$MUTATION_RATE_CLUSTER, 
                vertical=T, add=T, pch=20, col=c("red", "blue", "green"), 
                method="jitter", cex=2, jitter=.10)
    title = paste (gene, ", " , signif(kt$p.value, 4))
    title (main=title, xlab="Mutation Clusters", ylab="Hypermethylation")
    if (kt$p.value < 0.05) {
      cat ("** Significant Difference for gene:  ", gene, 
           ", p-value:  ", signif(kt$p.value, 4), "\n")
    }
    summary = data.frame (GENE=gene, P_VALUE=kt$p.value, TEST="Kruskal")
    return (summary)
  }, error = function(ex) {
    #cat("An error was detected.  Data was probably missing for gene:  ", gene, ".\n");
    #print(ex);
  })
}

# Compare Methylation of Specified Gene in High v. Highest Clusters
compareTwoWay <- function (gene, merged_df) {
  tryCatch({
    local_df = subset(merged_df, MUTATION_RATE_CLUSTER %in% c("2_HIGH", "3_HIGHEST"))
    local_df = transform(local_df, MUTATION_RATE_CLUSTER= factor(MUTATION_RATE_CLUSTER))
    
    wt = wilcox.test(local_df[[gene]] ~ factor(local_df$MUTATION_RATE_CLUSTER), data = merged_df)
    if (wt$p.value < 0.05) {
      cat ("** Significant Difference for gene:  ", gene, 
           ", p-value:  ", signif(wt$p.value, 4), "\n")
    }
    summary = data.frame (GENE=gene, P_VALUE=wt$p.value, TEST="Wilcoxon")
    return (summary)
  }, error = function(ex) {
    #cat("An error was detected.  Data was probably missing for gene:  ", gene, ".\n");
    #print(ex);
  })
}

#################################
# Read in Data Sets
df = read.delim("~/SugarSync/endo/data/dna_repair_meth.txt")
clin_df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")
merged_df = merge(df, clin_df)
genes = names(df)
genes = genes[2:length(genes)]

#################################
# Three Way Comparison
results = lapply (genes, compareThreeWay, merged_df)
results_df = do.call(rbind, results)

# Adjust for Multiple Hypothesis Testing (BH) and write out to file
library("multtest")
results_df = results_df[order(results_df$P_VALUE),]
adjp <- mt.rawp2adjp (results_df$P_VALUE, proc=c("BH"))
results_df$ADJUSTED_P_VALUE <- adjp$adjp[,"BH"]
write.table(results_df, file="dna_repair_3meth.txt", quote=F, sep="\t", row.names=F)

#################################
# Two Way Comparison
results = lapply (genes, compareTwoWay, merged_df)
results_df = do.call(rbind, results)

# Adjust for Multiple Hypothesis Testing (BH) and write out to file
results_df = results_df[order(results_df$P_VALUE),]
adjp <- mt.rawp2adjp (results_df$P_VALUE, proc=c("BH"))
results_df$ADJUSTED_P_VALUE <- adjp$adjp[,"BH"]
write.table(results_df, file="dna_repair_2meth.txt", quote=F, sep="\t", row.names=F)
