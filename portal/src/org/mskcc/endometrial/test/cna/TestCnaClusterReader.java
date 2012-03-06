package org.mskcc.endometrial.test.cna;

import junit.framework.TestCase;
import org.mskcc.endometrial.cna.CnaClusterReader;

import java.io.File;
import java.io.IOException;

/**
 * Tests the CNA Cluster Reader.
 */
public class TestCnaClusterReader extends TestCase {

    public void testCnaClusterReader() throws IOException {
        File cnaFile = new File ("test_data/cna_clusters_test.txt");
        CnaClusterReader reader = new CnaClusterReader(cnaFile);
        assertEquals("2", reader.getCnaClusterAssignment("TCGA-A5-A0G2"));
        assertEquals("3", reader.getCnaClusterAssignment("TCGA-A5-A0G5"));
    }
}
