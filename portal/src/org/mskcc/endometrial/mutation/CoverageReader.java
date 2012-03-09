package org.mskcc.endometrial.mutation;

import java.io.*;
import java.util.HashMap;

/**
 * Coverage Reader to read in # of bases adequately sequenced in each of the specified cases.
 * 
 */
public class CoverageReader {
    private HashMap<String, Long> coverageMap = new HashMap<String, Long>();

    /**
     * Constrcutor.
     *
     * @param coverageFile  Coverage File.
     * @throws IOException  IO Error.
     */
    public CoverageReader(File coverageFile) throws IOException {
        FileReader reader = new FileReader(coverageFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  // Skip Header
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String barCode = parts[0];
            long coveredBases = Long.parseLong(parts[1]);

            if (barCode.trim().length() > 0) {
                String caseId = MafUtil.extractCaseId(barCode);
                coverageMap.put(caseId, coveredBases);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    /**
     * Gets Coverage for the Specified Case ID.
     * @param caseId    Case ID.
     * @return  Coverage.
     */
    public Long getCoverage(String caseId) {
        Long value = coverageMap.get(caseId);
        if (value != null) {
            return value;
        } else {
            return new Long(0);
        }
    }
}