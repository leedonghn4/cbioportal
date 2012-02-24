#!/usr/bin/Rscript --no-save

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Restrict to Sequenced Cases Only
df = subset(df, SEQUENCED=="Y")

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

# Create AGE_GROUP Column
df = transform(df, AGE_GROUP="NA")
df$AGE_GROUP = factor(df$AGE_GROUP, levels = c("YOUNGER", "OLDER"))
df[df$age_at_initial_pathologic_diagnosis<60,]$AGE_GROUP="YOUNGER"
df[df$age_at_initial_pathologic_diagnosis>=60,]$AGE_GROUP="OLDER"

# Compare Frequency of PIK3CA Mutations in the Different Subtypes: Endometriod v. Serous
temp_df = subset(df, SUBTYPE %in% c("Endometriod", "Serous"))
temp_df = transform(temp_df, SUBTYPE=factor(temp_df$SUBTYPE))
t = table(temp_df$PIK3CA_MUTATED, temp_df$SUBTYPE)
pt = prop.table(t, 2)
f = fisher.test(t)
test0 = list (METRIC="Subtype", ENDOMETRIOD=pt[2,1], SEROUS=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Grades
t = table(df$PIK3CA_MUTATED, df$tumor_grade)
pt = prop.table(t, 2)
f = fisher.test(t)
test1 = list (METRIC="Grade", GRADE_1=pt[2,1], GRADE_2=pt[2,2], GRADE_3=pt[2,3], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Stages
t = table(df$PIK3CA_MUTATED, df$FIGO_STAGE)
pt = prop.table(t, 2)
f = fisher.test(t)
test2 = list (METRIC="Stage", STAGE_1=pt[2,1], STAGE_2=pt[2,2], STAGE_3=pt[2,3], STAGE_4=pt[2,4], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Age Groups
t = table(df$PIK3CA_MUTATED, df$AGE_GROUP)
pt = prop.table(t, 2)
f = fisher.test(t)
test3 = list (METRIC="Age", YOUNGER=pt[2,1], OLDER=pt[2,2], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")

# Compare Frequency of PIK3CA Mutations in the Different Mutation Rate Clusters
t = table(df$PIK3CA_MUTATED, df$MUTATION_RATE_CLUSTER)
pt = prop.table(t, 2)
f = fisher.test(t)
test4 = list (METRIC="Mutation Rate", LOW=pt[2,1], HIGH=pt[2,2], HIGHEST=pt[2,3], P_VALUE=signif(f$p.value, digits=4), TEST="Fisher's Exact")
