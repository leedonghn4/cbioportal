# Various Mutual Exclusivity Tests

computeMutExAll <- function (class_df) {
  row0 = computeMutEx0(class_df)
  row1 = computeMutEx1(class_df)
  row2 = computeMutEx2(class_df)
  row3 = computeMutEx3(class_df)
  row4 = computeMutEx4(class_df)
  row5 = computeMutEx5(class_df)
  row6 = computeMutEx6(class_df)
  row7 = computeMutEx7(class_df)
  row8 = computeMutEx8(class_df)
  mut_ex_df = rbind(row0, row1, row2, row3, row4, row5, row6, row7, row8)
  return (mut_ex_df)
}

computeMutEx0 <- function (class_df) {
  t = table(class_df$PTEN_MUTATED_0, class_df$PIK3CA_MUTATED_0)
  f = fisher.test(t)
  row = data.frame(METRIC="PTEN_MUTATED_0 v. PIK3CA_MUTATED_0", P_VALUE=f$p.value)
}

computeMutEx1 <- function (class_df) {
  t = table(class_df$PIK3CA_MUTATED_0, class_df$PIK3R1_MUTATED_0)
  f = fisher.test(t)
  row = data.frame(METRIC="PIK3CA_MUTATED_0 v. PIK3R1_MUTATED_0", P_VALUE=f$p.value)
}

computeMutEx2 <- function (class_df) {
  t = table(class_df$PIK3R1_MUTATED_0, class_df$PIK3R2_MUTATED_0)
  f = fisher.test(t)
  row = data.frame(METRIC="PIK3R1_MUTATED_0 v. PIK3R2_MUTATED_0", P_VALUE=f$p.value)
}

createTempMutColumns <- function(class_df, metric1, metric2) {
  class_df = transform(class_df, TEMP_MUTATED_1=0)
  class_df = transform(class_df, TEMP_MUTATED_2=0)
  class_df$TEMP_MUTATED_1 = ifelse(class_df[[metric1]]==2, 2, 0)
  class_df$TEMP_MUTATED_2 = ifelse(class_df[[metric2]]==2, 2, 0)
  return(class_df)
}

computeMutEx3 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PTEN_MUTATED_1", "PIK3CA_MUTATED_1")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  f = fisher.test(t)
  row = data.frame(METRIC="PTEN_MUTATED_1 v. PIK3CA_MUTATED_1", P_VALUE=f$p.value)
}

computeMutEx4 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PIK3CA_MUTATED_1", "PIK3R1_MUTATED_1")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  f = fisher.test(t)
  row = data.frame(METRIC="PIK3CA_MUTATED_1 v. PIK3R1_MUTATED_1", P_VALUE=f$p.value)
}

computeMutEx5 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PIK3R1_MUTATED_1", "PIK3R2_MUTATED_1")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  if (dim(t)[2]==2) {
    f = fisher.test(t)
    row = data.frame(METRIC="PIK3R1_MUTATED_1 v. PIK3R2_MUTATED_1", P_VALUE=f$p.value)
  } else {
    row = data.frame(METRIC="PIK3R1_MUTATED_1 v. PIK3R2_MUTATED_1", P_VALUE=1)
  }
}

computeMutEx6 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PTEN_MUTATED_3", "PIK3CA_MUTATED_3")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  print (t)
  f = fisher.test(t)
  row = data.frame(METRIC="PTEN_MUTATED_3 v. PIK3CA_MUTATED_3", P_VALUE=f$p.value)
}

computeMutEx7 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PIK3CA_MUTATED_3", "PIK3R1_MUTATED_3")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  f = fisher.test(t)
  row = data.frame(METRIC="PIK3CA_MUTATED_3 v. PIK3R1_MUTATED_3", P_VALUE=f$p.value)
}

computeMutEx8 <- function (class_df) {
  class_df = createTempMutColumns(class_df, "PIK3R1_MUTATED_3", "PIK3R2_MUTATED_3")
  t = table(class_df$TEMP_MUTATED_1, class_df$TEMP_MUTATED_2)
  if (dim(t)[2]==2) {
    f = fisher.test(t)
    row = data.frame(METRIC="PIK3R1_MUTATED_3 v. PIK3R2_MUTATED_3", P_VALUE=f$p.value)
  } else {
    row = data.frame(METRIC="PIK3R1_MUTATED_3 v. PIK3R2_MUTATED_3", P_VALUE=1)
  }
}