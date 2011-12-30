library(multtest)
df = read.delim ("../temp.txt", header=T)

altered_df = subset(df, df$FREQUENCY>.1)

results = altered_df[order(altered_df$OS_P),]

# Adjust for Multiple Hypothesis Testing (Benjamini Hochberg)
adjp <- mt.rawp2adjp (results$OS_P, proc=c("BH"))
results$ADJUSTED_OS_P_VALUE <- adjp$adjp[,"BH"]

write.table(results, file="results.txt", sep="\t", quote=F, row.names=F)