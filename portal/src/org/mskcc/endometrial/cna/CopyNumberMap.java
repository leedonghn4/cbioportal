package org.mskcc.endometrial.cna;

import org.mskcc.endometrial.mutation.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class takes as input a discretized GISTIC Copy Number File, and stores data for a target
 * set of genes.
 */
public class CopyNumberMap {
    private static final int START_INDEX = 3;
    private HashMap<String, String> copyNumberMap = new HashMap<String, String>();

    /**
     * Constructor.
     *
     * @param cnaFile       Copy Number File.
     * @param targetGeneSet Target Gene Set.
     * @throws java.io.IOException IO Error.
     */
    public CopyNumberMap(File cnaFile, HashSet<String> targetGeneSet) throws IOException {
        readCnaFile(cnaFile, targetGeneSet);
    }

    /**
     * Gets Copy Number Value Associated with the specified gene / case ID.
     *
     * @param geneSymbol Gene Symbol.
     * @param caseId     Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public String getCopyNumberValue(String geneSymbol, String caseId) {
        String key = createKey(geneSymbol, caseId);
        return copyNumberMap.get(key);
    }

    private void readCnaFile(File cnaFile, HashSet<String> targetGeneSet) throws IOException {
        FileReader reader = new FileReader(cnaFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        String colNames[] = line.trim().split("\t");

        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.trim().split("\t");
            String geneSymbol = parts[0];
            if (targetGeneSet.contains(geneSymbol)) {
                for (int i = START_INDEX; i<parts.length; i++) {
                    String barCode = colNames[i];
                    String caseId = MafUtil.extractCaseId(barCode);
                    String value = parts[i];
                    String key = createKey(geneSymbol, caseId);
                    copyNumberMap.put(key, value);
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private String createKey(String gene, String caseId) {
        return gene + ":" + caseId;
    }
}