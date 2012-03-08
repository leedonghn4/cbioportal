package org.mskcc.portal.mut_diagram.oncotator;

/**
 * Encapsulates Data re:  COSMIC Record.
 */
public class CosmicRecord {
    private String proteinChange;
    private int numRecords;

    public CosmicRecord(String proteinChange, int numRecords) {
        this.proteinChange = proteinChange;
        this.numRecords = numRecords;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }
}
