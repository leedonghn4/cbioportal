package org.mskcc.cgds.test.dao;

import java.util.Arrays;
import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoGeneSet;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.GeneSet;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.util.HashSet;
import java.util.ArrayList;

/**
 * JUnit Tests for DaoGeneSet.
 */
public class TestDaoGeneSet extends TestCase {

    /**
     * Tests DaoGeneSet.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public void testDaoGene() throws DaoException {
        ResetDatabase.resetDatabase();

        //  Add BRCA1 and BRCA2 Genes
        CanonicalGene brca1 = new CanonicalGene(672, "BRCA1",
                new HashSet<String>(Arrays.asList("BRCAI|BRCC1|BROVCA1|IRIS|PNCA4|PSCP|RNF53".split("\\|"))));
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        daoGeneOptimized.addGene(brca1);

        CanonicalGene brca2 = new CanonicalGene(675, "BRCA2",
                new HashSet<String>(Arrays.asList("BRCC2|BROVCA2|FACD|FAD|FAD1|FANCB|FANCD|FANCD1|GLM3|PNCA2".split("\\|"))));
        daoGeneOptimized.addGene(brca2);

        DaoGeneSet daoGeneSet = new DaoGeneSet();

        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        geneList.add(brca1);
        geneList.add(brca2);
        daoGeneSet.addGeneSet("HR Pathway", "Homologous Recombination Pathway", geneList);

        ArrayList<GeneSet> geneSetList = daoGeneSet.getAllGeneSets();
        assertEquals (1, geneSetList.size());

        GeneSet geneSet0 = geneSetList.get(0);
        assertEquals ("HR Pathway", geneSet0.getName());
        assertEquals ("Homologous Recombination Pathway", geneSet0.getDescription());
        geneList = geneSet0.getGeneList();
        assertEquals (2, geneList.size());

        validateBrca1(geneList.get(0));
        validateBrca2(geneList.get(1));
    }

    /**
     * Validates BRCA1.
     * @param gene Gene Object.
     */
    private void validateBrca1(CanonicalGene gene) {
        assertEquals("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals(672, gene.getEntrezGeneId());
    }

    /**
     * Validates BRCA2.
     * @param gene Gene Object.
     */
    private void validateBrca2(CanonicalGene gene) {
        assertEquals("BRCA2", gene.getHugoGeneSymbolAllCaps());
        assertEquals(675, gene.getEntrezGeneId());
    }
}