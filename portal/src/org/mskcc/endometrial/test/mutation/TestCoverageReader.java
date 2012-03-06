package org.mskcc.endometrial.test.mutation;

import junit.framework.TestCase;
import org.mskcc.endometrial.mutation.CoverageReader;

import java.io.File;
import java.io.IOException;

/**
 * Tests the Coverage Reader.
 */
public class TestCoverageReader extends TestCase {

    /**
     * Tests the Coverage Reader.
     *
     * @throws java.io.IOException IO Error.
     */
    public void testCoverageReader() throws IOException {
        File coverageFile = new File ("test_data/coverage_test.txt");
        CoverageReader reader = new CoverageReader(coverageFile);
        assertEquals(33040160, reader.getCoverage("TCGA-A5-A0G1").longValue());
        assertEquals(28911508, reader.getCoverage("TCGA-A5-A0GP").longValue());
    }
}
