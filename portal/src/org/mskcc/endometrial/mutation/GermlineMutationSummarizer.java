package org.mskcc.endometrial.mutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Reads in a Germline MAF File.
 *
 */
public class GermlineMutationSummarizer {
    private HashSet<String> mlh1I219VMutatedSet = new HashSet<String>();
    private HashSet<String> mlh1DelTTCSet = new HashSet<String>();

    public GermlineMutationSummarizer(File germlineMafFile) throws IOException {
        FileReader reader = new FileReader(germlineMafFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");

            String geneSymbol = parts[0];
            String barCode = parts[caseIdIndex];
            String variantClassification = parts[8];
            String referenceAllele = parts[10];
            String aaChange = null;
            try {
                aaChange = parts[47];
            } catch (ArrayIndexOutOfBoundsException e) {
                aaChange = null;
            }

            String barCodeParts[] = barCode.split("-");
            String caseId = extractCaseId(barCode, barCodeParts);

            if (geneSymbol.equals("MLH1")) {
                if (variantClassification.equalsIgnoreCase("In_Frame_Del")
                     & referenceAllele.equals("TTC")) {
                        mlh1DelTTCSet.add(caseId);
                } else if (variantClassification.equalsIgnoreCase("Missense_Mutation")) {
                    if (aaChange.equalsIgnoreCase("p.I219V")) {
                        mlh1I219VMutatedSet.add(caseId);
                    }
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private String extractCaseId(String barCode, String[] barCodeParts) {
        String caseId = null;
        try {
            caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
        } catch( ArrayIndexOutOfBoundsException e) {
            caseId = barCode;
        }
        return caseId;
    }

    public boolean isMlh1I219VMutated(String caseId) {
        if (mlh1I219VMutatedSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isMlh1DelTCC(String caseId) {
        if (mlh1DelTTCSet.contains(caseId)) {
            return true;
        } else {
            return false;
        }
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