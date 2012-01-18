package org.mskcc.endometrial.mutation;

import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Reads in a MAF File and Creates a BED File of Select Genes.
 */
public class BedCreator {

    public BedCreator(File mafFile, HashMap<String, String> sampleToClusterMap)
            throws IOException {
        HashSet<String> targetGeneSet = new HashSet<String>();
        targetGeneSet.add("PIK3CA");
        targetGeneSet.add("PTEN");
        FileReader reader = new FileReader(mafFile);
        ArrayList<String> bedLines = new ArrayList<String>();
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();

        while (line != null) {
            String parts[] = line.split("\t");

            String geneSymbol = parts[0];
            String barCode = parts[caseIdIndex];
            String variantClassification = parts[8];
            String chr = parts[4];
            String start = parts[5];
            String end = parts[6];
            String variantType = parts[9];
            String referenceAllele = parts[10];
            String tumorAllele = parts[12];

            String barCodeParts[] = barCode.split("-");
            String caseId = null;
            try {
                caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
            } catch( ArrayIndexOutOfBoundsException e) {
                caseId = barCode;
            }

            if (targetGeneSet.contains(geneSymbol)) {
                if (variantClassification.equalsIgnoreCase("Silent")) {

                } else {
                    String location = "chr" + chr + "\t" + start + "\t" + end;
                    bedLines.add(location);
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        System.out.println("track name=endo description=\"Endometrial TCGA\" visibility=2 color=255,0,0");
        for (String currentBedLine:  bedLines) {
            System.out.println(currentBedLine);
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
