package org.mskcc.endometrial.test.genomic;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.endometrial.cna.CopyNumberMap;
import org.mskcc.endometrial.genomic.GenomicMap;
import org.mskcc.endometrial.mutation.MutationMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Tests the Genomic Map.
 */
public class TestGenomicMap extends TestCase {
    
    public void testGenomicMap() throws IOException, DaoException {
        HashSet<String> targetGeneSet = new HashSet<String>();
        targetGeneSet.add("A1BG");
        targetGeneSet.add("A2M");
        File mafFile = new File("test_data/endo_maf_test.txt");
        MutationMap mutationMap = new MutationMap(mafFile, targetGeneSet);

        File cnaFile = new File ("test_data/endo_data_CNA.txt");
        CopyNumberMap copyNumberMap = new CopyNumberMap(cnaFile, targetGeneSet);

        GenomicMap genomicMap = new GenomicMap(mutationMap, copyNumberMap);
        assertEquals("1", genomicMap.getMutated_0("A1BG", "TCGA-D1-A162"));
        assertEquals("0", genomicMap.getMutated_0("BRCA1", "TCGA-D1-A162"));

        assertEquals("2", genomicMap.getMutated_1("A1BG", "TCGA-D1-A162"));
        assertEquals("0", genomicMap.getMutated_1("BRCA1", "TCGA-D1-A162"));

        assertEquals("p.E390K;p.A268V;", genomicMap.getMutated_2("A1BG", "TCGA-D1-A162"));
        assertEquals("0", genomicMap.getMutated_2("BRCA1", "TCGA-D1-A162"));

        assertEquals("1", genomicMap.getMutated_3("A1BG", "TCGA-D1-A162"));
        assertEquals("0", genomicMap.getMutated_3("BRCA1", "TCGA-D1-A162"));

        assertEquals("2", genomicMap.getCNA_0("A1BG", "TCGA-D1-A162"));
        assertEquals("NA", genomicMap.getCNA_0("BRCA1", "TCGA-D1-A162"));

        assertEquals("3", genomicMap.getAltered_1("A1BG", "TCGA-D1-A162"));
        assertEquals("0", genomicMap.getAltered_1("BRCA1", "TCGA-D1-A162"));
        
        ArrayList<String> dataFields = genomicMap.getDataFields("A1BG", "TCGA-D1-A162");
        assertEquals(6, dataFields.size());
    }
}
