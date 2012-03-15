compareClasses_0 <- function(metric, class1_df, class2_df, class1, class2) {
  num_class1_altered_no = nrow(class1_df[class1_df[[metric]]==0,])
  num_class1_altered_yes = nrow(class1_df[class1_df[[metric]]==1,])
  
  num_class2_altered2_no = nrow(class2_df[class2_df[[metric]]==0,])
  num_class2_altered_yes = nrow(class2_df[class2_df[[metric]]==1,])
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered2_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC=metric, class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}

compareClasses_1_3 <- function(metric, class1_df, class2_df, class1, class2) {
  num_class1_altered_no = nrow(class1_df[class1_df[[metric]]==0,])
  num_class1_altered_yes = nrow(class1_df[class1_df[[metric]]==2,])
  
  num_class2_altered2_no = nrow(class2_df[class2_df[[metric]]==0,])
  num_class2_altered_yes = nrow(class2_df[class2_df[[metric]]==2,])
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered2_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC=metric, class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}

compareAktClasses <- function(class1_df, class2_df, class1, class2) {
  num_class1_altered_yes = nrow(subset(class1_df, AKT1_MUTATED_0==1 | AKT2_MUTATED_0==1 | AKT3_MUTATED_0==1))
  num_class1_altered_no = nrow(class1_df) - num_class1_altered_yes
  
  num_class2_altered_yes = nrow(subset(class2_df, AKT1_MUTATED_0==1 | AKT2_MUTATED_0==1 | AKT3_MUTATED_0==1))
  num_class2_altered_no = nrow(class2_df) - num_class2_altered_yes
  
  t = matrix (c(num_class1_altered_yes,num_class1_altered_no,num_class2_altered_yes,num_class2_altered_no), nrow=2)
  pt = prop.table(t, 2)
  f = fisher.test(t)
  
  row = data.frame(METRIC="AKT123_MUTATED_0", class1=pt[1,1], class2=pt[1,2], P_VALUE=f$p.value)
  names(row) = c("METRIC", class1, class2, "P_VALUE")
  return (row)
}
