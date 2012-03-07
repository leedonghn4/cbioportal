package org.mskcc.endometrial.test.clinical;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.mskcc.endometrial.clinical.PrepareClinicalFile;

/**
 * Tests the Prepare Clinical File Class.
 */
public class TestPrepareClinicalFile extends TestCase {

    public void testPrepareClinicalFile() throws IOException {
        File clinFile = new File ("test_data/endo_clinical.txt");
        File msiFile = new File ("test_data/msi_test.txt");
        File somaticFile = new File ("test_data/endo_maf_test.txt");
        File germlineFile = new File ("test_data/mlh1_germline.txt");
        File cnaFile = new File("test_data/endo_data_CNA.txt");
        File cnaClusterFile = new File ("test_data/cna_clusters_test.txt");
        File mlh1MethFile = new File ("test_data/mlh1_meth_test.txt");
        File coverageFile = new File ("test_data/coverage_test.txt");
        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile
                (clinFile, msiFile, somaticFile, germlineFile, cnaFile,
                cnaClusterFile, mlh1MethFile, coverageFile, false);
        HashSet<String> sequencedCaseSet = prepareClinicalFile.getSequencedCaseSet();
        assertEquals (80, sequencedCaseSet.size());
        assertTrue(sequencedCaseSet.contains("TCGA-A5-A0G1"));
    }
}
