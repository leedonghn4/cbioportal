package org.mskcc.endometrial.genomic;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.endometrial.cna.CopyNumberMap;
import org.mskcc.endometrial.mutation.MutationMap;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorRecord;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorService;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Genomic Map Class.
 */
public class GenomicMap {
    private MutationMap mutationMap;
    private CopyNumberMap copyNumberMap;
    private static String ZERO = "0";
    private static String ONE = "1";
    private static String TWO = "2";
    private static String THREE = "3";
    private static String NA = "NA";

    /**
     * Constructor.
     *
     * @param mutationMap   Mutation Map Object.
     * @param copyNumberMap Copy Number Map Object.
     */
    public GenomicMap(MutationMap mutationMap, CopyNumberMap copyNumberMap) {
        this.mutationMap = mutationMap;
        this.copyNumberMap = copyNumberMap;
    }

    /**
     * Gets Column Headings for Specified Gene.
     * @param gene  Gene Symbol.
     * @return ArrayList of Column Headings.
     */
    public ArrayList<String> getColumnHeaders(String gene) {
        ArrayList<String> columnList = new ArrayList<String>();
        columnList.add(gene + "_MUTATED_0");
        columnList.add(gene + "_MUTATED_1");
        columnList.add(gene + "_MUTATED_2");
        columnList.add(gene + "_MUTATED_3");
        columnList.add(gene + "_CNA_0");
        columnList.add(gene + "_ALTERED_0");
        return columnList;
    }

    /**
     * Gets Data Fields for Specified Gene / Case ID Pair.
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return ArrayList of Column Fields.
     * @throws IOException  IO Error.
     * @throws DaoException Database Access Error.
     */
    public ArrayList<String> getDataFields(String gene, String caseId) throws IOException, DaoException {
        ArrayList<String> columnList = new ArrayList<String>();
        columnList.add(getMutated_0(gene, caseId));
        columnList.add(getMutated_1(gene, caseId));
        columnList.add(getMutated_2(gene, caseId));
        columnList.add(getMutated_3(gene, caseId));
        columnList.add(getCNA_0(gene, caseId));
        columnList.add(getAltered_1(gene, caseId));
        return columnList;
    }

    /**
     * Get Mutated_Level 0.
     * 0 = Not Mutated.
     * 1 = Mutated.
     *
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return mutation value.
     */
    public String getMutated_0 (String gene, String caseId) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations(gene, caseId);
        if (mutationList != null && mutationList.size() > 0) {
            return ONE;
        } else {
            return ZERO;
        }
    }

    /**
     * Get Mutated_Level 1.
     * 0 = Not Mutated.
     * 1 = Single Mutation.
     * 2 = More than one mutation.
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return mutation value.
     */
    public String getMutated_1 (String gene, String caseId) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations(gene, caseId);
        if (mutationList != null && mutationList.size() > 0) {
            if (mutationList.size() == 1) {
                return ONE;
            } else {
                return TWO;
            }
        } else {
            return ZERO;
        }
    }

    /**
     * Get Mutated_Level 2.
     * 0 = NOT MUTATED;
     * or semicolon-separated AA Change.
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return mutation value.
     */
    public String getMutated_2 (String gene, String caseId) throws IOException, DaoException {
        StringBuffer aaBuf = new StringBuffer();
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations(gene, caseId);
        if (mutationList != null && mutationList.size() > 0) {
            for (ExtendedMutation currentMutation:  mutationList) {
                OncotatorRecord oncotatorRecord = getOncotatorRecord(currentMutation);
                String proteinChange = oncotatorRecord.getProteinChange();
                aaBuf.append(proteinChange + ";");
            }
            return aaBuf.toString();
        } else {
            return ZERO;
        }
    }

    /**
     * Get Mutated_Level 3.
     * 0 = NOT MUTATED.
     * 1 = MUTATED and seen in COSMIC.
     * 2 = MUTATED, but not not see in COSMIC.
     *
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return mutation value.
     */
    public String getMutated_3 (String gene, String caseId) throws IOException, DaoException {
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations(gene, caseId);
        int numCosmicMatches = 0;
        if (mutationList != null && mutationList.size() > 0) {
            for (ExtendedMutation currentMutation:  mutationList) {
                OncotatorRecord oncotatorRecord = getOncotatorRecord(currentMutation);
                numCosmicMatches += oncotatorRecord.getNumExtactCosmicRecords();
            }
            if (numCosmicMatches > 0) {
                return TWO;
            } else {
                return ONE;
            }
        } else {
            return ZERO;
        }
    }

    /**
     * Get CNA_Level 1
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return GISTIC Discrete Value, e.g. -2, -1, 0, 1, 2.
     */
    public String getCNA_0(String gene, String caseId) {
        String cnaValue = copyNumberMap.getCopyNumberValue(gene, caseId);
        if (cnaValue != null) {
            return cnaValue;
        } else {
            return NA;
        }
    }

    /**
     * Get Altered_Level 1
     * 0 = Not Altered.
     * 1 = Altered by Mutation.
     * 2 = Altered by CNA (Amp, HomDel Only).
     * 3 = Altered by Mutation and CNA.
     * @param gene      Gene Symbol.
     * @param caseId    Case ID.
     * @return altered value.
     */
    public String getAltered_1 (String gene, String caseId) {
        String cnaValue = copyNumberMap.getCopyNumberValue(gene, caseId);
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations(gene, caseId);
        boolean isCopyNumberAltered = isCopyNumberAltered(cnaValue);
        boolean isMutated = isMutated(mutationList);
        if (isMutated & isCopyNumberAltered) {
            return THREE;
        } else if(isMutated) {
            return ONE;
        } else if(isCopyNumberAltered) {
            return TWO;
        } else {
            return ZERO;
        }
    }
    
    private OncotatorRecord getOncotatorRecord(ExtendedMutation mutation) throws IOException, DaoException {
        OncotatorService oncotatorService = OncotatorService.getInstance();
        OncotatorRecord oncotatorRecord = oncotatorService.getOncotatorAnnotation(mutation.getChr(),
                mutation.getStartPosition(), mutation.getEndPosition(), mutation.getRefAllele(),
                mutation.getObservedAllele());
        return oncotatorRecord;
    }
    
    private boolean isCopyNumberAltered (String value) {
        if (value != null) {
            if (value.equals("-2") || value.equals("2")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean isMutated (ArrayList<ExtendedMutation> mutationList) {
        if (mutationList != null && mutationList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}