package org.mskcc.endometrial.clinical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Reads in MSI Status for all Cases.
 */
public class MsiReader {
    private HashMap<String, String> msiMap = new HashMap<String, String>();

    /**
     * Constructor.
     * @param msiFile       MSI File.
     * @throws IOException  IO Error.
     */
    public MsiReader(File msiFile) throws IOException {
        FileReader reader = new FileReader(msiFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String barCode = parts[1];
            String msiClass = parts[11];

            if (barCode.trim().length()>0) {
                String caseId = extractCaseId(barCode);
                msiMap.put(caseId, msiClass);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    /**
     * Gets the MSI Status for the Specified Case.
     * @param caseId Case ID.
     * @return MSI Status.
     */
    public String getMsiStatus(String caseId) {
        return msiMap.get(caseId);
    }

    private String extractCaseId(String barCode) {
        String idParts[] = barCode.split("-");
        return "TCGA-" + idParts[0] + "-" + idParts[1];
    }
}