#!/usr/bin/Rscript --no-save

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Create new SUBTYPE Column that has Shorter Labels
df = transform(df, SUBTYPE="NA")
df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endometriod", "Mixed", "Serous"))
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endometriod"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endometriod"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endometriod"
df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

# Create new FIGO_STAGE Column
df = transform(df, FIGO_STAGE="NA")
df$FIGO_STAGE = factor(df$FIGO_STAGE, levels = c("Stage-I", "Stage-II", "Stage-III", "Stage-IV"))
df[df$X2009FIGOstageCorrected=="Stage I",]$FIGO_STAGE="Stage-I"
df[df$X2009FIGOstageCorrected=="Stage IA",]$FIGO_STAGE="Stage-I"
df[df$X2009FIGOstageCorrected=="Stage IB",]$FIGO_STAGE="Stage-I"
df[df$X2009FIGOstageCorrected=="Stage II",]$FIGO_STAGE="Stage-II"
df[df$X2009FIGOstageCorrected=="Stage IIIA",]$FIGO_STAGE="Stage-III"
df[df$X2009FIGOstageCorrected=="Stage IIIB",]$FIGO_STAGE="Stage-III"
df[df$X2009FIGOstageCorrected=="Stage IIIC",]$FIGO_STAGE="Stage-III"
df[df$X2009FIGOstageCorrected=="Stage IIIC1",]$FIGO_STAGE="Stage-III"
df[df$X2009FIGOstageCorrected=="Stage IIIC2",]$FIGO_STAGE="Stage-III"
df[df$X2009FIGOstageCorrected=="Stage IVA",]$FIGO_STAGE="Stage-IV"
df[df$X2009FIGOstageCorrected=="Stage IVB",]$FIGO_STAGE="Stage-IV"

df = transform(df, FIGO_STAGE_GROUP="NA")
df$FIGO_STAGE_GROUP = factor(df$FIGO_STAGE_GROUP, levels = c("Low-Stage", "High-Stage"))
df[df$FIGO_STAGE=="Stage-I",]$FIGO_STAGE_GROUP="Low-Stage"
df[df$FIGO_STAGE=="Stage-II",]$FIGO_STAGE_GROUP="Low-Stage"
df[df$FIGO_STAGE=="Stage-III",]$FIGO_STAGE_GROUP="High-Stage"
df[df$FIGO_STAGE=="Stage-IV",]$FIGO_STAGE_GROUP="High-Stage"

# Create AGE_GROUP Column
df = transform(df, AGE_GROUP="NA")
df$AGE_GROUP = factor(df$AGE_GROUP, levels = c("YOUNGER", "OLDER"))
df[df$age_at_initial_pathologic_diagnosis<60,]$AGE_GROUP="YOUNGER"
df[df$age_at_initial_pathologic_diagnosis>=60,]$AGE_GROUP="OLDER"

# Create GRADE_GROUP Column
df = transform(df, GRADE_GROUP="NA")
df$GRADE_GROUP = factor(df$GRADE_GROUP, levels = c("LOW-GRADE", "HIGH-GRADE"))
df[df$tumor_grade=="Grade 1",]$GRADE_GROUP="LOW-GRADE"
df[df$tumor_grade=="Grade 2",]$GRADE_GROUP="LOW-GRADE"
df[df$tumor_grade=="Grade 3",]$GRADE_GROUP="HIGH-GRADE"

# Restrict to Sequenced Cases Only
df = subset(df, SEQUENCED=="Y")

# Compare Frequency of PIK3CA Mutations in the Different Grades
# Restrict to Endometriod, Mut Low Only
temp_df = subset(df, SUBTYPE %in% c("Endometriod"))
temp_df = subset(temp_df, MUTATION_RATE_CLUSTER %in% c("1_LOW"))
t = table(temp_df$PIK3CA_MUTATED_0, temp_df$GRADE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test1 = list (METRIC="Grade", LOW_GRADE=pt[2,1], HIGH_GRADE=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Stages
t = table(temp_df$PIK3CA_MUTATED_0, temp_df$FIGO_STAGE)
pt = prop.table(t, 2)
f = fisher.test(t)
test2 = list (METRIC="Stage", STAGE_1=pt[2,1], STAGE_2=pt[2,2], STAGE_3=pt[2,3], STAGE_4=pt[2,4], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Stage Groups
t = table(temp_df$PIK3CA_MUTATED_0, temp_df$FIGO_STAGE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test3 = list (METRIC="Stage Group", LOW_STAGE=pt[2,1], HIGH_STAGE=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Age Groups
t = table(temp_df$PIK3CA_MUTATED_0, temp_df$AGE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test4 = list (METRIC="Age", YOUNGER=pt[2,1], OLDER=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of Biallelic PIK3CA Mutations in the Different Grades
# Restrict to Endometriod, Mut Low Only
df = transform(df, PIK3CA_BIALLEIC_MUTATION=0)
df[df$PIK3CA_MUTATED_1==2,]$PIK3CA_BIALLEIC_MUTATION=1

temp_df = subset(df, SUBTYPE %in% c("Endometriod"))
temp_df = subset(temp_df, MUTATION_RATE_CLUSTER %in% c("1_LOW"))
t = table(temp_df$PIK3CA_BIALLEIC_MUTATION, temp_df$GRADE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test5 = list (METRIC="Grade", LOW_GRADE=pt[2,1], HIGH_GRADE=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of Biallelic PIK3CA Mutations in the Different Stages
t = table(temp_df$PIK3CA_BIALLEIC_MUTATION, temp_df$FIGO_STAGE)
pt = prop.table(t, 2)
f = fisher.test(t)
test6 = list (METRIC="Stage", STAGE_1=pt[2,1], STAGE_2=pt[2,2], STAGE_3=pt[2,3], STAGE_4=pt[2,4], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of Biallelic PIK3CA Mutations in the Different Stage Groups
t = table(temp_df$PIK3CA_BIALLEIC_MUTATION, temp_df$FIGO_STAGE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test7 = list (METRIC="Stage Group", LOW_STAGE=pt[2,1], HIGH_STAGE=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Age Groups
t = table(temp_df$PIK3CA_BIALLEIC_MUTATION, temp_df$AGE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test8 = list (METRIC="Age", YOUNGER=pt[2,1], OLDER=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")
