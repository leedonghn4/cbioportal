package org.mskcc.endometrial.mutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Reads in a Germline MAF File, and extracts a subset of MMR Germline Variants.
 *
 */
public class GermlineMutationSummarizer {

    //  Individual Genes
    private HashSet<String> mlh1AnySet = new HashSet<String>();
    private HashSet<String> mlh1LikelyDeleteriousSet = new HashSet<String>();
    private HashSet<String> msh2AnySet = new HashSet<String>();
    private HashSet<String> msh2LikelyDeleteriousSet = new HashSet<String>();
    private HashSet<String> msh6AnySet = new HashSet<String>();
    private HashSet<String> msh6LikelyDeleteriousSet = new HashSet<String>();
    private HashSet<String> pms1AnySet = new HashSet<String>();
    private HashSet<String> pms1LikelyDeleteriousSet = new HashSet<String>();
    private HashSet<String> pms2AnySet = new HashSet<String>();
    private HashSet<String> pms2LikelyDeleteriousSet = new HashSet<String>();

    //  Individual Variants
    private HashSet<String> mlh1_I219V_Set = new HashSet<String>();
    private HashSet<String> mlh1_DEL_TCC_Set = new HashSet<String>();
    private HashSet<String> msh2_Q915R_Set = new HashSet<String>();
    private HashSet<String> msh2_N127S_Set = new HashSet<String>();
    private HashSet<String> msh6_G39E_Set = new HashSet<String>();
    private HashSet<String> pms2_K541E_Set = new HashSet<String>();
    private HashSet<String> pms2_P470S_Set = new HashSet<String>();
    private HashSet<String> pms2_G857A_Set = new HashSet<String>();
    private HashSet<String> pms2_T485K_Set = new HashSet<String>();
    private HashSet<String> pms2_T597S_Set = new HashSet<String>();
    private HashSet<String> pms2_R563L_Set = new HashSet<String>();
    private HashSet<String> pms2_R20Q_Set = new HashSet<String>();
    private HashSet<String> pms2_T511A_Set = new HashSet<String>();
    private HashSet<String> pms2_M622I_Set = new HashSet<String>();

    private HashSet<String> mmrGeneSet = new HashSet<String>();

    public GermlineMutationSummarizer(File germlineMafFile, boolean performSanityChecks) throws IOException {
        initMmrGeneSet();
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
            String aaChange = "NA";
            try {
                aaChange = parts[47];
            } catch (ArrayIndexOutOfBoundsException e) {
                aaChange = "NA";
            }

            String barCodeParts[] = barCode.split("-");
            String caseId = extractCaseId(barCode, barCodeParts);

            //  Skip silent mutations
            if (!variantClassification.equalsIgnoreCase("Silent")) {
                boolean likelyDeleterious = isLikelyDeleterious(variantClassification);

                if (mmrGeneSet.contains(geneSymbol)) {
                    if (geneSymbol.equals("MSH6") && aaChange.equals("p.R158C")) {
                        //  Skip over mis-annotated MSH6 gene
                        //  this gene is annotated by Wash U. as a missense mutation
                        //  but, according to Oncotator, it's actually a silent mutation.
                    } else {
                        extractMMRGene(geneSymbol, caseId, likelyDeleterious, aaChange, referenceAllele);
                    }
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        if (performSanityChecks) {
            performSanityCheck();
        }
    }

    public ArrayList<String> getColumnHeadings() {
        ArrayList <String> columnHeadings = new ArrayList<String>();
        columnHeadings.add("GERMLINE_MMR_ANY");
        columnHeadings.add("GERMLINE_MMR_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_MLH1_ANY");
        columnHeadings.add("GERMLINE_MLH1_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_MSH2_ANY");
        columnHeadings.add("GERMLINE_MSH2_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_MSH6_ANY");
        columnHeadings.add("GERMLINE_MSH6_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_PMS1_ANY");
        columnHeadings.add("GERMLINE_PMS1_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_PMS2_ANY");
        columnHeadings.add("GERMLINE_PMS2_LIKELY_DELETERIOUS");
        columnHeadings.add("GERMLINE_MLH1_I219V");
        columnHeadings.add("GERMINE_MLH1_DEL_TCC");
        columnHeadings.add("GERMLINE_MSH2_Q915R");
        columnHeadings.add("GERMLINE_MSH2_N127S");
        columnHeadings.add("GERMLINE_MSH6_R158C");
        columnHeadings.add("GERMLINE_MSH6_G39E");
        columnHeadings.add("GERMLINE_PMS2_K541E");
        columnHeadings.add("GERMLINE_PMS2_P470S");
        columnHeadings.add("GERMLINE_PMS2_G857A");
        columnHeadings.add("GERMLINE_PMS2_T485K");
        columnHeadings.add("GERMLINE_PMS2_R20Q");
        columnHeadings.add("GERMLINE_PMS2_T511A");
        columnHeadings.add("GERMLINE_PMS2_M622I");
        columnHeadings.add("GERMLINE_PMS2_T597S");
        columnHeadings.add("GERMLINE_PMS2_R563L");
        return columnHeadings;
    }

    public ArrayList<String> getValues(String caseId) {
        ArrayList <String> dataFields = new ArrayList<String>();

        // GERMLINE_MMR_ANY
        if (mlh1AnySet.contains(caseId) || msh2AnySet.contains(caseId) || msh6AnySet.contains(caseId)
                || pms1AnySet.contains(caseId) || pms2AnySet.contains(caseId)) {
            dataFields.add("1");
        } else {
            dataFields.add("0");
        }

        // GERMLINE_MMR_LIKELY_DELETERIOUS
        if (mlh1LikelyDeleteriousSet.contains(caseId) || msh2LikelyDeleteriousSet.contains(caseId)
                || msh6LikelyDeleteriousSet.contains(caseId)
                || pms1LikelyDeleteriousSet.contains(caseId)
                || pms2LikelyDeleteriousSet.contains(caseId)) {
            dataFields.add("1");
        } else {
            dataFields.add("0");
        }

        addDataField(dataFields, mlh1AnySet, caseId);
        addDataField(dataFields, mlh1LikelyDeleteriousSet, caseId);
        addDataField(dataFields, msh2AnySet, caseId);
        addDataField(dataFields, msh2LikelyDeleteriousSet, caseId);
        addDataField(dataFields, msh6AnySet, caseId);
        addDataField(dataFields, msh6LikelyDeleteriousSet, caseId);
        addDataField(dataFields, pms1AnySet, caseId);
        addDataField(dataFields, pms1LikelyDeleteriousSet, caseId);
        addDataField(dataFields, pms2AnySet, caseId);
        addDataField(dataFields, pms2LikelyDeleteriousSet, caseId);
        addDataField(dataFields, this.mlh1_I219V_Set, caseId);
        addDataField(dataFields, this.mlh1_DEL_TCC_Set, caseId);
        addDataField(dataFields, this.msh2_Q915R_Set, caseId);
        addDataField(dataFields, this.msh2_N127S_Set, caseId);
        addDataField(dataFields, this.msh6_G39E_Set, caseId);
        addDataField(dataFields, this.pms2_K541E_Set, caseId);
        addDataField(dataFields, this.pms2_P470S_Set, caseId);
        addDataField(dataFields, this.pms2_G857A_Set, caseId);
        addDataField(dataFields, this.pms2_T485K_Set, caseId);
        addDataField(dataFields, this.pms2_R20Q_Set, caseId);
        addDataField(dataFields, this.pms2_T511A_Set, caseId);
        addDataField(dataFields, this.pms2_M622I_Set, caseId);
        addDataField(dataFields, this.pms2_T597S_Set, caseId);
        addDataField(dataFields, this.pms2_R563L_Set, caseId);
        return dataFields;
    }

    //  Every HashSet should contain >0 cases.
    private void performSanityCheck() {
        checkSet(mlh1AnySet, "mlh1Any");
        checkSet(mlh1LikelyDeleteriousSet, "mlh1Likely");
        checkSet(msh2AnySet, "msh2Any");
        // Current MAF does not have any MSH2 Likley Deleterious;  so the statement below is commented out.
        //checkSet(msh2LikelyDeleteriousSet, "msh2Likley");
        checkSet(msh6AnySet, "msh6Any");
        checkSet(msh6LikelyDeleteriousSet, "msh6Likely");
        checkSet(pms1AnySet, "pms1Any");
        // Current MAF does not have any PMS1 Likley Deleterious;  so the statement below is commented out.
        // checkSet(pms1LikelyDeleteriousSet, "pms1Likely");
        checkSet(pms2AnySet, "pms2Any");
        checkSet(pms2LikelyDeleteriousSet, "psm2Likely");
        checkSet(mlh1_I219V_Set, "mlh1I219V");
        checkSet(mlh1_DEL_TCC_Set, "mlh1_DEL_TTC");
        checkSet(msh2_Q915R_Set, "msh2_Q915R");
        checkSet(msh2_N127S_Set, "msh2_N127S");
        checkSet(msh6_G39E_Set, "msh6_G39E");
        checkSet(pms2_K541E_Set, "psm2_K541E");
        checkSet(pms2_P470S_Set, "pms2_P470S");
        checkSet(pms2_G857A_Set, "pms2_G875A");
        checkSet(pms2_T485K_Set, "pms2_T485K");
        checkSet(pms2_T597S_Set, "pms2_T597S");
        checkSet(pms2_R563L_Set, "pms2_R563L");
        checkSet(pms2_R20Q_Set, "pms2_R20Q");
        checkSet(pms2_T511A_Set, "pms2_T551A");
        checkSet(pms2_M622I_Set, "pms2_M622I");
    }

    private void checkSet(HashSet<String> currentSet, String name) {
        if (currentSet.size() == 0) {
            throw new NullPointerException ("Case Set:  " + name + " is empty.");
        }
    }

    private void addDataField(ArrayList<String> dataFields, HashSet<String> mutationSet, String caseId) {
        if (mutationSet.contains(caseId)) {
            dataFields.add("1");
        } else {
            dataFields.add("0");
        }
    }

    private void extractMMRGene (String geneSymbol, String caseId, boolean likelyDeleterious, String aaChange,
            String refAllele) {
        if (geneSymbol.equals("MLH1")) {
            mlh1AnySet.add(caseId);
            if (likelyDeleterious) {
                mlh1LikelyDeleteriousSet.add(caseId);
            }
            if (aaChange.equals("p.I219V")) {
                mlh1_I219V_Set.add(caseId);
            } else if (refAllele.equals("TTC")) {
                mlh1_DEL_TCC_Set.add(caseId);    
            }
        } else if (geneSymbol.equals("MSH2")) {
            msh2AnySet.add(caseId);
            if (likelyDeleterious) {
                msh2LikelyDeleteriousSet.add(caseId);
            }
            if (aaChange.equals("p.Q915R")) {
                msh2_Q915R_Set.add(caseId);
            } else if (aaChange.equals("p.N127S")) {
                msh2_N127S_Set.add(caseId);
            }
        } else if (geneSymbol.equals("MSH6")) {
            msh6AnySet.add(caseId);
            if (likelyDeleterious) {
                msh6LikelyDeleteriousSet.add(caseId);
            }
            if (aaChange.equals("p.G39E")) {
                msh6_G39E_Set.add(caseId);
            }
        } else if (geneSymbol.equals("PMS1")) {
            pms1AnySet.add(caseId);
            if (likelyDeleterious) {
                pms1LikelyDeleteriousSet.add(caseId);
            }
        } else if (geneSymbol.equals("PMS2")) {
            pms2AnySet.add(caseId);
            if (likelyDeleterious) {
                pms2LikelyDeleteriousSet.add(caseId);
            }
            //  Check for Individual Variants
            if (aaChange.equals("p.K541E")) {
                pms2_K541E_Set.add(caseId);
            } else if (aaChange.equals("p.P470S")) {
                pms2_P470S_Set.add(caseId);
            } else if (aaChange.equals("p.G857A")) {
                pms2_G857A_Set.add(caseId);
            } else if (aaChange.equals("p.T485K")) {
               pms2_T485K_Set.add(caseId); 
            } else if (aaChange.equals("p.T597S")) {
               pms2_T597S_Set.add(caseId);
            } else if (aaChange.equals("p.R563L")) {
               pms2_R563L_Set.add(caseId);
            } else if (aaChange.equals("p.R20Q")) {
               pms2_R20Q_Set.add(caseId);
            } else if (aaChange.equals("p.T511A")) {
               pms2_T511A_Set.add(caseId);
            } else if (aaChange.equals("p.M622I")) {
                pms2_M622I_Set.add(caseId);
            }
        }
    }

    private boolean isLikelyDeleterious(String variantClassification) {
        if (variantClassification.equalsIgnoreCase("Nonsense_Mutation")) {
            return true;
        } else if (variantClassification.equalsIgnoreCase("In_Frame_Del")) {
            return true;
        } else if (variantClassification.equalsIgnoreCase("Frame_Shift_Del")) {
            return true;
        } else if (variantClassification.equalsIgnoreCase("Frame_Shift_Ins")) {
            return true;
        } else {
            return false;
        }
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

    private void initMmrGeneSet() {
        mmrGeneSet.add("MLH1");
        mmrGeneSet.add("MSH2");
        mmrGeneSet.add("MSH6");
        mmrGeneSet.add("PMS1");
        mmrGeneSet.add("PMS2");
    }
}