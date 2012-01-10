package org.mskcc.endometrial.mutation;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Reads in a MAF File and Summarizes by Case.
 *
 * Possible variant classifications are:
 *
 * 3'UTR
 * 5'UTR
 * Frame_Shift_Del
 * Frame_Shift_Ins
 * In_Frame_Del
 * In_Frame_Ins
 * Missense_Mutation
 * Nonsense_Mutation
 * Nonstop_Mutation
 * RNA
 * Silent
 * Splice_Site
 * Variant_Classification
 */
public class MutationSummarizer {
    private HashSet<String> sequencedCaseSet = new HashSet<String>();
    private HashMap<String, Integer> nonSilentMutationMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> inDelMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> silentMutationMap = new HashMap<String, Integer>();
    private HashSet<String> indelKeywordSet = new HashSet<String>();
    private HashSet<String> mlh1MutatedSet = new HashSet<String>();

    public MutationSummarizer(File mafFile) throws IOException {
        indelKeywordSet.add("Frame_Shift_Del");
        indelKeywordSet.add("Frame_Shift_Ins");
        indelKeywordSet.add("In_Frame_Del");
        indelKeywordSet.add("In_Frame_Ins");

        FileReader reader = new FileReader(mafFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");

            String geneSymbol = parts[0];
            String barCode = parts[caseIdIndex];
            String barCodeParts[] = barCode.split("-");
            String caseId = null;
            try {
                caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
            } catch( ArrayIndexOutOfBoundsException e) {
                caseId = barCode;
            }
            if (!sequencedCaseSet.contains(caseId)) {
                sequencedCaseSet.add(caseId);
            }

            String variantType = parts[8];
            if (variantType.equalsIgnoreCase("Silent")) {
                incrementCounterMap(caseId, silentMutationMap);
            } else {
                if (geneSymbol.equalsIgnoreCase("MLH1")) {
                    mlh1MutatedSet.add(caseId);
                }
                if (indelKeywordSet.contains(variantType)) {
                    incrementCounterMap(caseId, inDelMap);
                } else {
                    incrementCounterMap(caseId, nonSilentMutationMap);
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    public boolean isMlh1Mutated(String caseId) {
        if (mlh1MutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public int getNonSilentMutationMap (String caseId) {
        if (sequencedCaseSet.contains(caseId)) {
            if (nonSilentMutationMap.containsKey(caseId)) {
                return nonSilentMutationMap.get(caseId);
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public int getInDelCount (String caseId) {
        if (sequencedCaseSet.contains(caseId)) {
            if (inDelMap.containsKey(caseId)) {
                return inDelMap.get(caseId);
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public int getSilentMutationCount (String caseId) {
        if (sequencedCaseSet.contains(caseId)) {
            if (silentMutationMap.containsKey(caseId)) {
                return silentMutationMap.get(caseId);
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public HashSet<String> getSequencedCaseSet() {
        return sequencedCaseSet;
    }
    
    private void incrementCounterMap(String caseId, HashMap<String, Integer> countMap) {
        int currentCounter = 1;
        if (countMap.containsKey(caseId)) {
            currentCounter = countMap.get(caseId) + 1;
        }
        countMap.put(caseId, currentCounter);
    }

    private int getCaseIdIndex(String headerLine) {
        String parts[] = headerLine.split("\t");
        for (int i=0; i<parts.length; i++) {
            String headerName = parts[i];
            if (headerName.equals("Tumor_Sample_Barcode")) {
                return i;
            }
        }
        return -1;
    }
}