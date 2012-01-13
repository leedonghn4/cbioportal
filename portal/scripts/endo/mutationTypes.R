#!/usr/bin/Rscript --no-save
library(gridExtra)

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Restrict to Sequenced Cases
df = subset(df, SEQUENCED=="Y")

# Remove the one outlier that has 0 mutations
df = subset(sub_df, TOTAL_SNV_COUNT>1)

# Create new Percent Columns
df = transform(df, TG_PERCENT = TG_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TC_PERCENT = TC_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TA_PERCENT = TA_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CT_PERCENT = CT_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CG_PERCENT = CG_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CA_PERCENT = CA_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TOTAL_PERCENT=TG_PERCENT + TC_PERCENT + TA_PERCENT + CT_PERCENT + CG_PERCENT + CA_PERCENT)

# Sort by TOTAL_SNV_COUNT
sub_df = sub_df[order (sub_df$TOTAL_SNV_COUNT, decreasing=T),]

p1 = qplot(1:nrow(sub_df), TG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p2 = qplot(1:nrow(sub_df), TC_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p3 = qplot(1:nrow(sub_df), TA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p4 = qplot(1:nrow(sub_df), CT_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p5 = qplot(1:nrow(sub_df), CG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p6 = qplot(1:nrow(sub_df), CA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")

grid.arrange(p1, p2, p3, p4, p5, p6, nrow=2)

p = qplot(1:nrow(sub_df), CA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p = p + opts(title="C>A/G>T Transversions")
p = p + scale_y_continuous("Fractions of all SNVs")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p

p = qplot(1:nrow(sub_df), CG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity")
p = p + opts(title="C>G/G>C Transversions")
p = p + scale_y_continuous("Fractions of all SNVs")
p = p + scale_x_continuous("All Sequenced Cases (Ordered by Mutation Count)")
p

# Box Plot
kt = kruskal.test(CA_PERCENT ~ factor(MUTATION_RATE_CATEGORY), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), CA_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("C>A/G>T Transversion Ratio") 
the_title = paste("InDel Ratios Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p

kt = kruskal.test(CT_PERCENT ~ factor(MUTATION_RATE_CATEGORY), data = sub_df)
p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), CT_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("C>T/G>A Ratio") 
the_title = paste("InDel Ratios Across Mutation Categories\nKruskall-Wallace:  ", signif(kt$p.value, 4))
p = p + opts(title=the_title)
p
