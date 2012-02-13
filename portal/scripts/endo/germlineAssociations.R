#!/usr/bin/Rscript --no-save

library("multtest")

######################################################################################
# Compare Function:  Performs Fisher's Exact Test and Returns Results in a Data Frame
######################################################################################
compare <- function(metric, sub_df) {
  print(metric)
  t = table (sub_df[[metric]], sub_df$MUTATION_RATE_CLUSTER)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  summary = data.frame (METRIC=metric, 
                  MUT_LOW=round(pt[2,1], 2), 
                  MUT_HIGH=round(pt[2,2], 2), 
                  MUT_HIGHEST=round(pt[2,3], 2),
                  P_VALUE=round(f$p.value, digits=4), 
                  TEST="Fisher's Exact")
  return (summary)
}

# Define all metrics
metrics = c("GERMLINE_MMR_ANY", "GERMLINE_MMR_LIKELY_DELETERIOUS", "GERMLINE_MLH1_ANY",
	"GERMLINE_MLH1_LIKELY_DELETERIOUS", "GERMLINE_MSH2_ANY",
	"GERMLINE_MSH6_ANY", "GERMLINE_MSH6_LIKELY_DELETERIOUS", "GERMLINE_PMS1_ANY", 
	"GERMLINE_PMS2_ANY", "GERMLINE_PMS2_LIKELY_DELETERIOUS",
	"GERMLINE_MLH1_I219V", "GERMINE_MLH1_DEL_TCC", "GERMLINE_MSH2_Q915R", "GERMLINE_MSH2_N127S",
	"GERMLINE_MSH6_G39E", "GERMLINE_PMS2_K541E", "GERMLINE_PMS2_P470S",
	"GERMLINE_PMS2_G857A", "GERMLINE_PMS2_T485K", "GERMLINE_PMS2_R20Q", 
	"GERMLINE_PMS2_T511A", "GERMLINE_PMS2_M622I", "GERMLINE_PMS2_T597S", "GERMLINE_PMS2_R563L")

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Restrict to Sequenced Cases Only
sub_df = subset(df, SEQUENCED=="Y")

# Remove outliers
# Case with exactly 1 mutation
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

# Call Compare on all metrics, and place into unified dataframe
results = lapply (metrics, compare, sub_df)
results_df = do.call(rbind, results)

# Sort Results by P-Value
results_df = results_df[order(results_df$P_VALUE),]

# Adjust for Multiple Hypothesis Testing (Benjamini Hochberg)
adjp <- mt.rawp2adjp (results_df$P_VALUE, proc=c("BH"))
results_df$ADJUSTED_P_VALUE <- adjp$adjp[,"BH"]