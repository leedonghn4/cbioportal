#!/usr/bin/Rscript --no-save

######################################################################################
# Compare Function
######################################################################################
compare <- function(sub_df, category, metric) {
  t = table (category, sub_df$MUTATION_RATE_CLUSTER)
  pt = prop.table(t, 2)
  f = fisher.test(t)

  summary = list (METRIC=metric, 
                  MUT_LOW=round(pt[2,1], 2), 
                  MUT_HIGH=round(pt[2,2], 2), 
                  MUT_HIGHEST=round(pt[2,3], 2),
                  P_VALUE=round(f$p.value, digits=4), 
                  TEST="Fisher's Exact")
  return (summary)
}

# GERMLINE_MMR_ANY
# GERMLINE_MMR_LIKELY_DELETERIOUS
# GERMLINE_MLH1_ANY                      
# GERMLINE_MLH1_LIKELY_DELETERIOUS
# GERMLINE_MSH2_ANY
# GERMLINE_MSH2_LIKELY_DELETERIOUS        
# GERMLINE_MSH6_ANY
# GERMLINE_MSH6_LIKELY_DELETERIOUS
# GERMLINE_PMS1_ANY                    
# GERMLINE_PMS1_LIKELY_DELETERIOUS
# GERMLINE_PMS2_ANY
# GERMLINE_PMS2_LIKELY_DELETERIOUS        
# GERMLINE_MLH1_I219V
# GERMINE_MLH1_DEL_TCC
# GERMLINE_MSH2_Q915R                    
# GERMLINE_MSH2_N127S
# GERMLINE_MSH6_R158C
# GERMLINE_MSH6_G39E                      
# GERMLINE_PMS2_K541E
# GERMLINE_PMS2_P470S
# GERMLINE_PMS2_G857A                     
# GERMLINE_PMS2_T485K
# GERMLINE_PMS2_R20Q
# GERMLINE_PMS2_T511A                     
# GERMLINE_PMS2_M622I
# GERMLINE_PMS2_T597S
# GERMLINE_PMS2_R563L

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Restrict to Sequenced Cases Only
sub_df = subset(df, SEQUENCED=="Y")

# Remove outliers
# Case with exactly 1 mutation
sub_df = subset(sub_df, TOTAL_SNV_COUNT>1)

summary0 = compare(sub_df, sub_df$GERMLINE_MMR_ANY, "GERMLINE_MMR_ANY")
summary1 = compare(sub_df, sub_df$GERMLINE_MMR_LIKELY_DELETERIOUS, "GERMLINE_MMR_LIKELY_DELETERIOUS");
summary2 = compare(sub_df, sub_df$GERMLINE_MLH1_ANY, "GERMLINE_MLH1_ANY");
summary3 = compare(sub_df, sub_df$GERMLINE_MLH1_LIKELY_DELETERIOUS, "GERMLINE_MLH1_LIKELY_DELETERIOUS");
summary4 = compare(sub_df, sub_df$GERMLINE_MSH2_ANY, "GERMLINE_MSH2_ANY");
# no data on MSH2 Likely Deleterious
# summary5 = compare(sub_df, sub_df$GERMLINE_MSH2_LIKELY_DELETERIOUS, "GERMLINE_MSH2_LIKELY_DELETERIOUS");
summary6 = compare(sub_df, sub_df$GERMLINE_MSH6_ANY, "GERMLINE_MSH6_ANY");
summary7 = compare(sub_df, sub_df$GERMLINE_MSH6_LIKELY_DELETERIOUS, "GERMLINE_MSH6_LIKELY_DELETERIOUS");
summary8 = compare(sub_df, sub_df$GERMLINE_PMS1_ANY, "GERMLINE_PMS1_ANY");
# no data on PSM1 Likely Deleterious
# summary9 = compare(sub_df, sub_df$GERMLINE_PMS1_LIKELY_DELETERIOUS, "GERMLINE_PMS1_LIKELY_DELETERIOUS");
summary10 = compare(sub_df, sub_df$GERMLINE_PMS2_ANY, "GERMLINE_PMS2_ANY");
summary11 = compare(sub_df, sub_df$GERMLINE_PMS2_LIKELY_DELETERIOUS, "GERMLINE_PMS2_LIKELY_DELETERIOUS");
summary12 = compare(sub_df, sub_df$GERMLINE_MLH1_I219V, "GERMLINE_MLH1_I219V");
summary13 = compare(sub_df, sub_df$GERMINE_MLH1_DEL_TCC, "GERMINE_MLH1_DEL_TCC");
summary14 = compare(sub_df, sub_df$GERMLINE_MSH2_Q915R, "GERMLINE_MSH2_Q915R");
summary15 = compare(sub_df, sub_df$GERMLINE_MSH2_N127S, "GERMLINE_MSH2_N127S");
summary16 = compare(sub_df, sub_df$GERMLINE_MSH6_R158C, "GERMLINE_MSH6_R158C");
summary17 = compare(sub_df, sub_df$GERMLINE_MSH6_G39E, "GERMLINE_MSH6_G39E");
summary18 = compare(sub_df, sub_df$GERMLINE_PMS2_K541E, "GERMLINE_PMS2_K541E");
summary19 = compare(sub_df, sub_df$GERMLINE_PMS2_P470S, "GERMLINE_PMS2_P470S");
summary20 = compare(sub_df, sub_df$GERMLINE_PMS2_G857A, "GERMLINE_PMS2_G857A");
summary21 = compare(sub_df, sub_df$GERMLINE_PMS2_T485K, "GERMLINE_PMS2_T485K");
summary22 = compare(sub_df, sub_df$GERMLINE_PMS2_R20Q, "GERMLINE_PMS2_R20Q");
summary23 = compare(sub_df, sub_df$GERMLINE_PMS2_T511A, "GERMLINE_PMS2_T511A");
summary24 = compare(sub_df, sub_df$GERMLINE_PMS2_M622I, "GERMLINE_PMS2_M622I");
summary25 = compare(sub_df, sub_df$GERMLINE_PMS2_T597S, "GERMLINE_PMS2_T597S");
summary26 = compare(sub_df, sub_df$GERMLINE_PMS2_R563L, "GERMLINE_PMS2_R563L");
