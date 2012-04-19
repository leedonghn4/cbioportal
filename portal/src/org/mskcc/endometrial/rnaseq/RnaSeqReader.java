package org.mskcc.endometrial.rnaseq;

import org.mskcc.endometrial.mutation.MafUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Reads in Case IDs from an RNA Seq File.
 */
public class RnaSeqReader {
    private HashSet<String> caseMap = new HashSet<String>();
    
    public RnaSeqReader(File file) throws IOException {
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();

        String headers[] = line.split("\t");
        for (int i=0; i<headers.length; i++) {
            String caseId = MafUtil.extractCaseId(headers[i]);
            caseMap.add(caseId);
        }
    }
    
    public boolean hasRnaReqData(String caseId) {
        if (caseMap.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }
}
