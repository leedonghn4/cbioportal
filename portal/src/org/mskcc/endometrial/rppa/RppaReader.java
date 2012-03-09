package org.mskcc.endometrial.rppa;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Reads in an RPPA Data File, and extracts data on few targeted antibodies.
 */
public class RppaReader {
    private HashMap<String, String> rppaMap = new HashMap<String, String>();
    private static String NA = "NA";    

    /**
     * Constructor.
     * 
     * @param rppaFile      RPPA Data File.
     * @throws IOException  IO Error.
     */
    public RppaReader(File rppaFile) throws IOException {
        FileReader reader = new FileReader(rppaFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        String colNames[] = line.split("\t");

        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String slideId = parts[0]; 
            for (int i = 1; i<parts.length; i++) {
                String caseId = colNames[i];
                String value = parts[i];
                String key = createKey(slideId, caseId);
                rppaMap.put(key, value);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }
    
    public ArrayList<String> getRppaHeaders() {
        ArrayList<String> colHeaders = new ArrayList<String>();
        colHeaders.add("AKT_pS473");
        colHeaders.add("AKT_pT308");
        colHeaders.add("PTEN_PROTEIN_LEVEL");
        return colHeaders;
    }
    
    public ArrayList<String> getDataValues(String caseId) {
        ArrayList<String> dataFields = new ArrayList<String>();
        dataFields.add(get_AKT_pS473(caseId));
        dataFields.add(get_AKT_pT308(caseId));
        dataFields.add(get_PTEN_PROTEIN_LEVEL(caseId));
        return dataFields;
    }

    public String get_AKT_pS473(String caseId) {
        String key = createKey("GBL9016658", caseId);
        return getDataValue(key);
    }

    public String get_AKT_pT308(String caseId) {
        String key = createKey("GBL9016659", caseId);
        return getDataValue(key);
    }

    public String get_PTEN_PROTEIN_LEVEL(String caseId) {
        String key = createKey("GBL9016719", caseId);
        return getDataValue(key);
    }
    
    private String getDataValue(String key) {
        String value = rppaMap.get(key);
        if (value == null) {
            return NA;
        } else {
            return value;
        }
    }

    private String createKey(String slideId, String caseId) {
        return slideId + ":" + caseId; 
    }
}
