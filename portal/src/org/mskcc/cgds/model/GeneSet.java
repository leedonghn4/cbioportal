package org.mskcc.cgds.model;

import java.util.ArrayList;

/**
 * Encapsulates Information regarding a Gene Set.
 */
public class GeneSet {
    private String name;
    private String description;
    private ArrayList<CanonicalGene> geneList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<CanonicalGene> getGeneList() {
        return geneList;
    }

    public void setGeneList(ArrayList<CanonicalGene> geneList) {
        this.geneList = geneList;
    }
}
