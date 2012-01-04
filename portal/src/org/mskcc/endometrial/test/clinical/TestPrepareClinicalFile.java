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
        File mafFile = new File ("test_data/data_mutations_extended.txt");
        File cnaClusterFile = new File ("test_data/cna_clusters_test.txt");
        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile(clinFile, msiFile, mafFile, cnaClusterFile);
        String dfsMonths = prepareClinicalFile.getDfsMonths("TCGA-A5-A0GJ");
        assertEquals ("0.39", dfsMonths);
        String osMonths = prepareClinicalFile.getOsMonths("TCGA-A5-A0GJ");
        assertEquals ("47.54", osMonths);

        dfsMonths = prepareClinicalFile.getDfsMonths("TCGA-EY-A1G7");
        assertEquals("3.12", dfsMonths);
        osMonths = prepareClinicalFile.getOsMonths("TCGA-EY-A1G7");
        assertEquals("6.21", osMonths);

        dfsMonths = prepareClinicalFile.getDfsMonths("TCGA-AP-A1E3");
        assertEquals("NA", dfsMonths);

        osMonths = prepareClinicalFile.getOsMonths("TCGA-B5-A0K7");
        assertEquals("17.05", osMonths);

        dfsMonths = prepareClinicalFile.getDfsMonths("TCGA-B5-A0K7");
        assertEquals("10.55", dfsMonths);

        String msiStatus = prepareClinicalFile.getMsiStatus("TCGA-AP-A051");
        assertEquals("MSI-H", msiStatus);

        HashSet<String> sequencedCaseSet = prepareClinicalFile.getSequencedCaseSet();
        assertEquals (1, sequencedCaseSet.size());
        assertTrue(sequencedCaseSet.contains("TCGA-AA-3664"));
    }
}
