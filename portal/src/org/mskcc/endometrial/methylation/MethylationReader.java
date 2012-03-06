package org.mskcc.endometrial.methylation;

import org.mskcc.endometrial.mutation.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Reads in a Methylation Data File.
 */
public class MethylationReader {
    private HashMap<String, String> hyperMethylatedMap = new HashMap<String, String>();

    /**
     * Constructor.
     * @param methylationFile   Methylation File.
     * @throws IOException      IO Error.
     */
    public MethylationReader(File methylationFile) throws IOException {
        FileReader reader = new FileReader(methylationFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String barCode = parts[0];
            String value = parts[1];

            if (barCode.trim().length()>0) {
                String caseId = MafUtil.extractCaseId(barCode);
                hyperMethylatedMap.put(caseId, value);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    /**
     * Gets the Methylation Status for the Specified Case ID.
     * @param caseId Case ID.
     * @return methylation status.
     */
    public String getMethylationStatus(String caseId) {
        return hyperMethylatedMap.get(caseId);
    }
}
