#!/usr/bin/Rscript --no-save
library(gridExtra)
library(ggplot2)

pdf("report.pdf", width=9, height=7)

compareMutationTypes <-function(test, sub_df, mutationCategory) {
	cat(test)
	wilcox.test(mutationCategory ~ sub_df$MUTATION_RATE_CLUSTER)
	t = t.test(mutationCategory ~ sub_df$MUTATION_RATE_CLUSTER)
	print(t)
	diff = t$estimate[[1]] - t$estimate[[2]]
	print(paste("Difference:  ", diff))
}

# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

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
sub_df = df[order (df$TOTAL_SNV_COUNT, decreasing=F),]

p1 = qplot(1:nrow(sub_df), TG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)
p2 = qplot(1:nrow(sub_df), TC_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)
p3 = qplot(1:nrow(sub_df), TA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)
p4 = qplot(1:nrow(sub_df), CT_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)
p5 = qplot(1:nrow(sub_df), CG_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)
p6 = qplot(1:nrow(sub_df), CA_PERCENT, data=sub_df, geom="bar", colour=MUTATION_RATE_CLUSTER, stat="identity") + opts(legend.position = "none") + xlab("All Cases") + ylim(0,1)

grid.arrange(p1, p2, p3, p4, p5, p6, nrow=2)

# Box Plots
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER=="3_HIGHEST" | MUTATION_RATE_CLUSTER=="2_HIGH")

compareMutationTypes("TG", sub_df, sub_df$TG_PERCENT)
compareMutationTypes("TC", sub_df, sub_df$TC_PERCENT)
compareMutationTypes("TA", sub_df, sub_df$TA_PERCENT)
compareMutationTypes("CT", sub_df, sub_df$CT_PERCENT)
compareMutationTypes("CG", sub_df, sub_df$CG_PERCENT)
compareMutationTypes("CA", sub_df, sub_df$CA_PERCENT)

theme_set(theme_grey(8))
p1 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), TG_PERCENT))
p1 = p1 + geom_boxplot(outlier.size =0) 
p1 = p1 + geom_jitter(position=position_jitter(w=0.1), size=2)
p1 = p1 + xlab("Mutation Rate Category") 
p1 = p1 + ylab("Ratio") 
the_title1 = "T->G"
p1 = p1 + opts(title=the_title1)
p1 = p1 +  opts(axis.text.x = theme_text(size = 8))

p2 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), TC_PERCENT))
p2 = p2 + geom_boxplot(outlier.size =0) 
p2 = p2 + geom_jitter(position=position_jitter(w=0.1), size=2)
p2 = p2 + xlab("Mutation Rate Category") 
p2 = p2 + ylab("Ratio") 
the_title2 = "T->C"
p2 = p2 + opts(title=the_title2)
p2 = p2 +  opts(axis.text.x = theme_text(size = 8))

p3 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), TA_PERCENT))
p3 = p3 + geom_boxplot(outlier.size =0) 
p3 = p3 + geom_jitter(position=position_jitter(w=0.1), size=2)
p3 = p3 + xlab("Mutation Rate Category") 
p3 = p3 + ylab("Ratio") 
the_title3 = "T->A"
p3 = p3 + opts(title=the_title3)
p3 = p3 +  opts(axis.text.x = theme_text(size = 8))

p4 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), CT_PERCENT))
p4 = p4 + geom_boxplot(outlier.size =0) 
p4 = p4 + geom_jitter(position=position_jitter(w=0.1), size=2)
p4 = p4 + xlab("Mutation Rate Category") 
p4 = p4 + ylab("Ratio") 
the_title4 = "C->T"
p4 = p4 + opts(title=the_title4)
p4 = p4 +  opts(axis.text.x = theme_text(size = 8))

p5 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), CG_PERCENT))
p5 = p5 + geom_boxplot(outlier.size =0) 
p5 = p5 + geom_jitter(position=position_jitter(w=0.1), size=2)
p5 = p5 + xlab("Mutation Rate Category") 
p5 = p5 + ylab("Ratio") 
the_title5 = "C->G"
p5 = p5 + opts(title=the_title5)
p5 = p5 +  opts(axis.text.x = theme_text(size = 8))

p6 = ggplot(sub_df,aes(factor(MUTATION_RATE_CLUSTER), CA_PERCENT))
p6 = p6 + geom_boxplot(outlier.size =0) 
p6 = p6 + geom_jitter(position=position_jitter(w=0.1), size=2)
p6 = p6 + xlab("Mutation Rate Category") 
p6 = p6 + ylab("Ratio") 
the_title6 = "C->A"
p6 = p6 + opts(title=the_title6)
p6 = p6 +  opts(axis.text.x = theme_text(size = 8))

grid.arrange(p1, p2, p3, p4, p5, p6, nrow=2)

dev.off()