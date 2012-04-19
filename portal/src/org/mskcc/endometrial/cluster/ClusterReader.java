package org.mskcc.endometrial.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Reads in a Cluster Assignment Class.
 */
public class ClusterReader {
    private HashMap<String, ArrayList<String>> clusterMap = 
            new HashMap<String, ArrayList<String>>();
    private ArrayList<String> headerList = new ArrayList<String>();
    
    public ClusterReader(File clusterFile) throws IOException {
        System.out.println("Reading clusters from:  " + clusterFile);
        FileReader reader = new FileReader(clusterFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        
        String headers[] = line.split("\t");
        for (int i=1; i<headers.length; i++) {
            headerList.add(headers[i]);                  
        }

        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String caseId = parts[0];

            ArrayList<String> valueList = new ArrayList<String>();
            for (int i=1; i<parts.length; i++) {
                valueList.add(parts[i]);
            }
            clusterMap.put(caseId, valueList);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    public ArrayList<String> getHeaderList() {
        return headerList;
    }

    public ArrayList<String> getValueList(String caseId) {
        return clusterMap.get(caseId);
    }
}
