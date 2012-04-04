package org.mskcc.endometrial.cna;

import org.mskcc.endometrial.mutation.MafUtil;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Reads in a GISTIC Gene Thresholded File, and summarizes data for each patient.
 */
public class CnaSummarizer {
    private String[] colNames;
    private HashSet<String> geneSet = new HashSet<String>();
    private HashSet<String> caseSet = new HashSet<String>();
    private HashMap<String, Integer> cnaCount1Map = new HashMap<String, Integer>();
    private HashMap<String, Integer> cnaCount2Map = new HashMap<String, Integer>();

    public CnaSummarizer(File gisticFile) throws IOException {
        FileReader reader = new FileReader(gisticFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        colNames = line.trim().split("\t");
        initCounters(colNames);

        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.trim().split("\t");
            String geneSymbol = parts[0];
            if (!geneSet.contains(geneSymbol)) {
                geneSet.add(geneSymbol);
                iterateThroughAllCases(parts);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }
    
    public HashSet<String> getGisticCaseSet() {
        return caseSet;
    }

    public boolean hasCnaData(String caseId) {
        if (caseSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }

    public int getCna1Count(String caseId) {
        if (cnaCount1Map.containsKey(caseId)) {
            return cnaCount1Map.get(caseId);
        } else {
            return -1;
        }
    }

    public int getCna2Count(String caseId) {
        if (cnaCount2Map.containsKey(caseId)) {
            return cnaCount2Map.get(caseId);
        } else {
            return -1;
        }
    }

    private void incrementCounters(String value, String caseId) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue != 0) {
                incrementCounter(caseId, cnaCount1Map);
            }
            if (isAmpOrHomDel(intValue)) {
                incrementCounter(caseId, cnaCount2Map);
            }
        } catch (NumberFormatException e) {
            //  If NA, do not increment any counters.
        }
    }

    private void initCounters(String colNames[]) {
        for (int i=2; i<colNames.length; i++) {
            String caseId = MafUtil.extractCaseId(colNames[i]);
            cnaCount1Map.put(caseId, 0);
            cnaCount2Map.put(caseId, 0);
            caseSet.add(caseId);
        }
    }

    private void iterateThroughAllCases(String[] parts) {
        for (int i=2; i<parts.length; i++) {
            String caseId = MafUtil.extractCaseId(colNames[i]);
            String value = parts[i];
            incrementCounters(value, caseId);
        }
    }

    private void incrementCounter (String caseId, HashMap<String, Integer> countMap) {
        int currentCount = 1;
        if (countMap.containsKey(caseId)) {
            currentCount = countMap.get(caseId) + 1;
        }
        countMap.put(caseId, currentCount);
    }

    private boolean isAmpOrHomDel(int value) {
        return Math.abs(value) == 2;
    }
}