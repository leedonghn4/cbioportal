package org.mskcc.portal.mut_diagram.oncotator;

import java.util.ArrayList;

/**
 * Encapsulate a Single Record from OncotatorRecord.
 */
public class OncotatorRecord {
    private String key;
    private String gene;
    private String genomeChange;
    private String proteinChange;
    private String variantClassification;
    private int exonAffected;
    private String cosmicOverlappingMutations;
    private ArrayList<CosmicRecord> cosmicRecordList = new ArrayList<CosmicRecord>();

    public OncotatorRecord(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getVariantClassification() {
        return variantClassification;
    }

    public void setVariantClassification(String variantClassification) {
        this.variantClassification = variantClassification;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getGenomeChange() {
        return genomeChange;
    }

    public void setGenomeChange(String genomeChange) {
        this.genomeChange = genomeChange;
    }

    public int getExonAffected() {
        return exonAffected;
    }

    public void setExonAffected(int exonAffected) {
        this.exonAffected = exonAffected;
    }

    public String getCosmicOverlappingMutations() {
        if (cosmicOverlappingMutations == null) {
            return "--";
        } else {
            return cosmicOverlappingMutations;
        }
    }
    
    public ArrayList<CosmicRecord> getCosmicRecords() {
        return cosmicRecordList;
    }

    public void setCosmicOverlappingMutations(String cosmicOverlappingMutations) {
        this.cosmicOverlappingMutations = cosmicOverlappingMutations;
        this.cosmicRecordList = extractCosmicRecords(cosmicOverlappingMutations);
    }

    /**
     * Gets the Number of COSMIC Records that Match this variant exactly.
     * Matching is determined by protein change string.
     * @return boolean.
     */
    public int getNumExtactCosmicRecords() {
        for (CosmicRecord cosmicRecord:  cosmicRecordList) {
            if (proteinChange.equals(cosmicRecord.getProteinChange())) {
                return cosmicRecord.getNumRecords();
            }
        }
        return 0;
    }
    
    private ArrayList<CosmicRecord> extractCosmicRecords(String cosmicString) {
        //  String looks like this:  p.?(4)|p.Y27_N212>Y(2)|p.Y27fs*1(2)|p.E40*(1)
        ArrayList<CosmicRecord> localList = new ArrayList<CosmicRecord>();
        if (cosmicString != null && cosmicString.trim().length() > 0) {
            String parts[] = cosmicString.split("\\|");
            for (String part:  parts) {
                part = part.replace("(", "#");
                part = part.replace(")", "#");
                String subparts[] = part.split("#");
                String aaPart = subparts[0];
                Integer count = Integer.parseInt(subparts[1]);
                CosmicRecord record = new CosmicRecord(aaPart, count);
                localList.add(record);
            }
        }
        return localList;
    }
}