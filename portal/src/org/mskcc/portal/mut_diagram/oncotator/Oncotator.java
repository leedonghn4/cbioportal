package org.mskcc.portal.mut_diagram.oncotator;

/**
 * Encapsulate a Single Record from Oncotator.
 */
public class Oncotator {
    private String key;
    private String gene;
    private String genomeChange;
    private String proteinChange;
    private String variantClassification;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
