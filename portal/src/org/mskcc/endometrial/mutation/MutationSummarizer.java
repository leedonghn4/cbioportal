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
    private HashSet<String> mlh1MutatedSet = new HashSet<String>();
    private HashSet<String> tp53MutatedSet = new HashSet<String>();
    private HashSet<String> ptenMutatedSet = new HashSet<String>();
    private HashSet<String> pik3caMutatedSet = new HashSet<String>();
    private HashSet<String> krasMutatedSet = new HashSet<String>();
    private HashMap<String, Integer> tgMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> tcMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> taMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> ctMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> cgMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> caMap = new HashMap<String, Integer>();

    public MutationSummarizer(File mafFile) throws IOException {
        FileReader reader = new FileReader(mafFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");

            String geneSymbol = parts[0];
            String barCode = parts[caseIdIndex];
            String variantClassification = parts[8];
            String variantType = parts[9];
            String referenceAllele = parts[10];
            String tumorAllele = parts[12];


            String barCodeParts[] = barCode.split("-");
            String caseId = null;
            try {
                caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
            } catch( ArrayIndexOutOfBoundsException e) {
                caseId = barCode;
            }

            String mutation = referenceAllele + tumorAllele;
            if (variantType.equals("SNP")) {
                if (mutation.equals("TG") || mutation.equals("AC")) {
                    incrementCounterMap(caseId, tgMap);
                } else if(mutation.equals("TC") || mutation.equals("AG")) {
                    //  Transitions
                    incrementCounterMap(caseId, tcMap);
                } else if(mutation.equals("TA") || mutation.equals("AT")) {
                    incrementCounterMap(caseId, taMap);
                } else if(mutation.equals("CT") || mutation.equals("GA")) {
                    //  Transitions
                    incrementCounterMap(caseId, ctMap);
                } else if (mutation.equals("CG") || mutation.equals("GC")) {
                    incrementCounterMap(caseId,cgMap);
                } else if (mutation.equals("CA") || mutation.equals("GT")) {
                    incrementCounterMap(caseId, caMap);
                } else {
                    throw new IllegalArgumentException ("Mutation not recognized:  " + mutation);
                }
            }

            if (!sequencedCaseSet.contains(caseId)) {
                sequencedCaseSet.add(caseId);
            }

            if (variantClassification.equalsIgnoreCase("Silent")) {
                incrementCounterMap(caseId, silentMutationMap);
            } else {
                if (geneSymbol.equalsIgnoreCase("MLH1")) {
                    mlh1MutatedSet.add(caseId);
                } else if (geneSymbol.equals("TP53")) {
                    tp53MutatedSet.add(caseId);
                } else if (geneSymbol.equals("PTEN")) {
                    ptenMutatedSet.add(caseId);
                } else if (geneSymbol.equals("PIK3CA")) {
                    pik3caMutatedSet.add(caseId);
                } else if (geneSymbol.equals("KRAS")) {
                    krasMutatedSet.add(caseId);
                }
                if (variantType.equals("DEL") || variantType.equals("INS")) {
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

    public boolean isTp53Mutated(String caseId) {
        if (tp53MutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPtenMutated(String caseId) {
        if (ptenMutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPik3caMutated(String caseId) {
        if (pik3caMutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isKrasMutated(String caseId) {
        if (krasMutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public int getTGMutationCount(String caseId) {
        return getCount(caseId, tgMap);
    }

    public int getTCMutationCount(String caseId) {
        return getCount(caseId, tcMap);
    }
    
    public int getTAMutationCount(String caseId) {
        return getCount(caseId, taMap);
    }

    public int getCTMutationCount(String caseId) {
        return getCount(caseId, ctMap);
    }
    
    public int getCGMutationCount(String caseId) {
        return getCount(caseId, cgMap);
    }

    public int getCAMutationCount(String caseId) {
        return getCount(caseId, caMap);
    }

    public int getNonSilentMutationCount(String caseId) {
        return getCount(caseId, nonSilentMutationMap);
    }

    public int getInDelCount (String caseId) {
        return getCount(caseId, inDelMap);
    }

    public int getSilentMutationCount (String caseId) {
        return getCount(caseId, silentMutationMap);
    }

    private int getCount(String caseId, HashMap<String, Integer> counterMap) {
        if (sequencedCaseSet.contains(caseId)) {
            if (counterMap.containsKey(caseId)) {
                return counterMap.get(caseId);
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