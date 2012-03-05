package org.mskcc.endometrial.mutation;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class takes as input a MAF File, and stores mutation data for a target
 * set of genes.
 */
public class MutationMap {
    private HashMap<String, ArrayList<ExtendedMutation>> mutationHashMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();

    /**
     * Constructor.
     *
     * @param mafFile       MAF File.
     * @param targetGeneSet Target Gene Set.
     * @throws IOException IO Error.
     */
    public MutationMap(File mafFile, HashSet<String> targetGeneSet) throws IOException {
        readMaf(mafFile, targetGeneSet);
    }

    /**
     * Gets all Mutation Records Associated with the specified gene / case ID.
     *
     * @param geneSymbol Gene Symbol.
     * @param caseId     Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList<ExtendedMutation> getMutations(String geneSymbol, String caseId) {
        String key = createKey(geneSymbol, caseId);
        return mutationHashMap.get(key);
    }

    private void readMaf(File mafFile, HashSet<String> targetGeneSet) throws IOException {
        FileReader reader = new FileReader(mafFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = MafUtil.getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String geneSymbol = parts[0];
            String chr = parts[4];
            long startPos = getLongValue(parts[5]);
            long endPos = getLongValue(parts[6]);
            String barCode = parts[caseIdIndex];
            String variantClassification = parts[8];
            String referenceAllele = parts[10];
            String tumorAllele = parts[12];
            String caseId = MafUtil.extractCaseId(barCode);

            if (!variantClassification.equalsIgnoreCase("Silent")) {
                //  Only store data for target genes
                if (targetGeneSet.contains(geneSymbol)) {
                    ExtendedMutation mutation = createMutationRecord(geneSymbol, chr, startPos, endPos,
                            variantClassification, referenceAllele, tumorAllele);
                    storeToMap(geneSymbol, caseId, mutation);
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private void storeToMap(String geneSymbol, String caseId, ExtendedMutation mutation) {
        String key = createKey(geneSymbol, caseId);
        if (mutationHashMap.containsKey(key)) {
            ArrayList<ExtendedMutation> mutationList = mutationHashMap.get(key);
            mutationList.add(mutation);
        } else {
            ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();
            mutationList.add(mutation);
            mutationHashMap.put(key, mutationList);
        }
    }

    private long getLongValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            System.err.println("Could not parse:  " + value);
            return -1;
        }
    }

    private ExtendedMutation createMutationRecord(String geneSymbol, String chr, long startPos,
        long endPos, String variantClassification, String referenceAllele, String tumorAllele) {
        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setGene(new CanonicalGene(-1, geneSymbol));
        mutation.setMutationType(variantClassification);
        mutation.setChr(chr);
        mutation.setStart(startPos);
        mutation.setEnd(endPos);
        mutation.setReferenceAllele(referenceAllele);
        mutation.setTumorAllele(tumorAllele);
        return mutation;
    }

    private String createKey(String gene, String caseId) {
        return gene + ":" + caseId;
    }
}