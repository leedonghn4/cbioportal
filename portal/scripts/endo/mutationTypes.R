#!/usr/bin/Rscript --no-save
library(gridExtra)
library(ggplot2)

pdf("report.pdf", width=9, height=7)

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_unified.txt")

# Restrict to Sequenced Cases
df = subset(df, SEQUENCED=="Y")

# Remove the one outlier that has 0 mutations
df = subset(df, TOTAL_SNV_COUNT>1)

# Create new Percent Columns
df = transform(df, TG_PERCENT = TG_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TC_PERCENT = TC_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TA_PERCENT = TA_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CT_PERCENT = CT_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CG_PERCENT = CG_COUNT/TOTAL_SNV_COUNT)
df = transform(df, CA_PERCENT = CA_COUNT/TOTAL_SNV_COUNT)
df = transform(df, TOTAL_PERCENT=TG_PERCENT + TC_PERCENT + TA_PERCENT + CT_PERCENT + CG_PERCENT + CA_PERCENT)

# Sort by TOTAL_SNV_COUNT
sub_df = df[order (df$TOTAL_SNV_COUNT, decreasing=T),]

p1 = qplot(1:nrow(sub_df), TG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")
p2 = qplot(1:nrow(sub_df), TC_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")
p3 = qplot(1:nrow(sub_df), TA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")
p4 = qplot(1:nrow(sub_df), CT_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")
p5 = qplot(1:nrow(sub_df), CG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")
p6 = qplot(1:nrow(sub_df), CA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CATEGORY, stat="identity") + opts(legend.position = "none") + xlab("All Cases")

grid.arrange(p1, p2, p3, p4, p5, p6, nrow=2)

# Compare HIGHEST v. HIGH Directly
local_df_1 = subset(sub_df, MUTATION_RATE_CATEGORY=="1_HIGHEST", select=c(TOTAL_SNV_COUNT, CASE_ID, TG_PERCENT, TC_PERCENT, TA_PERCENT, CT_PERCENT, CG_PERCENT, CA_PERCENT))
melted_df_1 = melt(local_df_1, id=c("CASE_ID", "TOTAL_SNV_COUNT"), variable_name="metric")
x1 = reorder(melted_df_1$CASE_ID, melted_df_1$TOTAL_SNV_COUNT)
#p1 = ggplot(melted_df_1, aes(x=x1, y=value, group=metric, fill=metric)) + geom_area() 
p1 = ggplot(melted_df_1, aes(x=x1, y=value, fill=metric)) + geom_bar() 
p1 = p1 + opts(title="Highest Mutation Group")
p1 = p1 + opts(axis.text.x = theme_blank())
p1 = p1 + xlab("All Cases")
p1 = p1 + opts(legend.position = "none") 
p1 = p1 + ylab("Proportion of all SNVs")

local_df_2 = subset(sub_df, MUTATION_RATE_CATEGORY=="2_HIGH", select=c(TOTAL_SNV_COUNT, CASE_ID, TG_PERCENT, TC_PERCENT, TA_PERCENT, CT_PERCENT, CG_PERCENT, CA_PERCENT))
melted_df_2 = melt(local_df_2, id=c("CASE_ID", "TOTAL_SNV_COUNT"), variable_name="metric")
x2 = reorder(melted_df_2$CASE_ID, melted_df_2$TOTAL_SNV_COUNT)
#p2 = ggplot(melted_df_2, aes(x=x2, y=value, group=metric, fill=metric)) + geom_area()
p2 = ggplot(melted_df_2, aes(x=x2, y=value, fill=metric)) + geom_bar() 
p2 = p2 + opts(title="High Mutation Group")
p2 = p2 + opts(axis.text.x = theme_blank())
p2 = p2 + xlab("All Cases")
p2 = p2 + ylab("Proportion of all SNVs")

grid.arrange(p1, p2, nrow=1)


# Box Plots
sub_df = subset(sub_df, MUTATION_RATE_CATEGORY=="1_HIGHEST"  | MUTATION_RATE_CATEGORY=="2_HIGH")

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), TG_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "T->G"
p = p + opts(title=the_title)
p

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), TC_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "T->C"
p = p + opts(title=the_title)
p

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), TA_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "T->A"
p = p + opts(title=the_title)
p

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), CT_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "C->T"
p = p + opts(title=the_title)
p

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), CG_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "C->G"
p = p + opts(title=the_title)
p

p = ggplot(sub_df,aes(factor(MUTATION_RATE_CATEGORY), CA_PERCENT))
p = p + geom_boxplot(outlier.size =0) 
p = p + geom_jitter(position=position_jitter(w=0.1), size=3)
p = p + xlab("Mutation Rate Category") 
p = p + ylab("Ratio") 
the_title = "C->A"
p = p + opts(title=the_title)
p

dev.off()