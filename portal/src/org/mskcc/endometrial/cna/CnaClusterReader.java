package org.mskcc.endometrial.cna;

import org.mskcc.endometrial.mutation.MafUtil;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Reads in a File of Copy Number Cluster Assignments.
 */
public class CnaClusterReader {
    private HashMap<String, String> cnaClusterAssignmentMap = new HashMap<String, String>();
    private HashSet<String> cluster1Set = new HashSet<String>();
    private HashSet<String> cluster2Set = new HashSet<String>();
    private HashSet<String> cluster3Set = new HashSet<String>();

    /**
     * Constructor.
     * @param cnaClusterFile    CNA Cluster Assignment File.
     * @throws IOException      IO Error.
     */
    public CnaClusterReader(File cnaClusterFile) throws IOException {
        FileReader reader = new FileReader(cnaClusterFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String barCode = parts[0];
            String cnaCluster = parts[1];

            if (barCode.trim().length()>0) {
                String caseId = MafUtil.extractCaseId(barCode);
                cnaClusterAssignmentMap.put(caseId, cnaCluster);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        createClusterSets();
    }

    /**
     * Gets Cluster Assignment for Specified Case.
     * @param caseId Case ID.
     * @return cluster assignment value.
     */
    public String getCnaClusterAssignment(String caseId) {
        return cnaClusterAssignmentMap.get(caseId);
    }

    /**
     * Gets all Cases in Cluster 1.
     * @return Hash Set of Case IDs.
     */
    public HashSet<String> getCluster1Set() {
        return cluster1Set;
    }

    /**
     * Gets all Cases in Cluster 2.
     * @return Hash Set of Case IDs.
     */
    public HashSet<String> getCluster2Set() {
        return cluster2Set;
    }

    /**
     * Gets all Cases in Cluster 3.
     * @return Hash Set of Case IDs.
     */
    public HashSet<String> getCluster3Set() {
        return cluster3Set;
    }

    private void createClusterSets() {
        Iterator<String> caseIterator = cnaClusterAssignmentMap.keySet().iterator();
        while (caseIterator.hasNext()) {
            String caseId = caseIterator.next();
            String clusterId = cnaClusterAssignmentMap.get(caseId);
            if (clusterId.equals("1")) {
                cluster1Set.add(caseId);
            } else if (clusterId.equals("2")) {
                cluster2Set.add(caseId);
            } else if (clusterId.equals("3")) {
                cluster3Set.add(caseId);
            }
        }
    }
}