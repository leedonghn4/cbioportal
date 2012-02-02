package org.mskcc.endometrial.test.mutation;

import junit.framework.TestCase;
import org.mskcc.endometrial.mutation.GermlineMutationSummarizer;
import org.mskcc.endometrial.mutation.MutationSummarizer;

import java.io.File;
import java.io.IOException;

/**
 * Tests the MutationSummarizer.
 */
public class TestGermlineMutationSummarizer extends TestCase {

    public void testMutationSummarizer() throws IOException {
        File mafFile = new File ("test_data/mlh1_germline.txt");
        GermlineMutationSummarizer mutationSummarizer =
                new GermlineMutationSummarizer(mafFile);
    }
}
