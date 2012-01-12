package org.mskcc.endometrial.test.mutation;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;

import org.mskcc.endometrial.mutation.MutationSummarizer;

/**
 * Tests the MutationSummarizer.
 */
public class TestMutationSummarizer extends TestCase {

    public void testMutationSummarizer() throws IOException {
        File mafFile = new File ("test_data/endo_maf_test.txt");
        MutationSummarizer mutationSummarizer = new MutationSummarizer(mafFile);

        //  A total of 23
        assertEquals (5, mutationSummarizer.getSilentMutationCount("TCGA-A5-A0G1"));
        assertEquals (18, mutationSummarizer.getNonSilentMutationCount("TCGA-A5-A0G1"));

        //  A total of 23
        assertEquals(0, mutationSummarizer.getTGMutationCount("TCGA-A5-A0G1"));
        assertEquals(2, mutationSummarizer.getTCMutationCount("TCGA-A5-A0G1"));
        assertEquals(3, mutationSummarizer.getTAMutationCount("TCGA-A5-A0G1"));
        assertEquals(12, mutationSummarizer.getCTMutationCount("TCGA-A5-A0G1"));
        assertEquals(0, mutationSummarizer.getCGMutationCount("TCGA-A5-A0G1"));
        assertEquals(6, mutationSummarizer.getCAMutationCount("TCGA-A5-A0G1"));
    }
}
