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

        assertEquals (5, mutationSummarizer.getSilentMutationCount("TCGA-A5-A0G1"));
        assertEquals (18, mutationSummarizer.getNonSilentMutationCount("TCGA-A5-A0G1"));
    }
}
