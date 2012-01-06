package org.mskcc.endometrial.test.cna;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

import org.mskcc.endometrial.cna.CnaSummarizer;

/**
 * Tests the CnaSummarizer.
 */
public class TestCnaSummarizder extends TestCase {

    public void testCnaSummarizer() throws IOException {
        File cnaFile = new File ("test_data/cna_sample.txt");
        CnaSummarizer cnaSummarizer = new CnaSummarizer(cnaFile);
        assertEquals (0, cnaSummarizer.getCna1Count("TCGA-02-0001"));
        assertEquals (0, cnaSummarizer.getCna2Count("TCGA-02-0001"));

        assertEquals (1, cnaSummarizer.getCna1Count("TCGA-02-0004"));
        assertEquals (0, cnaSummarizer.getCna2Count("TCGA-02-0004"));
        
        assertEquals (1, cnaSummarizer.getCna1Count("TCGA-08-0385"));
        assertEquals (1, cnaSummarizer.getCna2Count("TCGA-08-0385"));
    }
}
