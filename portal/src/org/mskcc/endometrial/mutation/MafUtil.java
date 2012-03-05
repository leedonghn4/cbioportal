package org.mskcc.endometrial.mutation;

/**
 * Misc. Utility Functions for Processing MAF Files.
 */
public class MafUtil {

    /**
     * Parses the Header Line of a MAF File and Returns Column Index for Case IDs.
     * @param headerLine    Header Line.
     * @return  column indx.
     */
    public static int getCaseIdIndex(String headerLine) {
        String parts[] = headerLine.split("\t");
        for (int i = 0; i < parts.length; i++) {
            String headerName = parts[i];
            if (headerName.equals("Tumor_Sample_Barcode")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Given a Bar Code ID, e.g. TCGA-D1-A162-01A-11D-A122-09, extract
     * the Case ID, e.g. TCGA-D1-A162.
     *
     * @param barCode   Bar Code String.
     * @return case ID.
     */
    public static String extractCaseId(String barCode) {
        String caseId = null;
        String barCodeParts[] = barCode.split("-");
        try {
            caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            caseId = barCode;
        }
        return caseId;
    }
}