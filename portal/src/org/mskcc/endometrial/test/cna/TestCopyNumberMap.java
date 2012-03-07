package org.mskcc.endometrial.test.cna;

import junit.framework.TestCase;
import org.mskcc.endometrial.cna.CopyNumberMap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Tests the Copy Number Map.
 */
public class TestCopyNumberMap extends TestCase {

    public void testCopyNumberMap() throws IOException {
        File cnaFile = new File ("test_data/endo_data_CNA.txt");
        HashSet<String> targetGeneSet = new HashSet<String>();
        targetGeneSet.add("ACAP3");
        targetGeneSet.add("C1orf159");

        CopyNumberMap copyNumberMap = new CopyNumberMap(cnaFile, targetGeneSet);

        //  Check Edge Conditions
        assertEquals("0", copyNumberMap.getCopyNumberValue("ACAP3", "1B6EC202-CFF2-4EA1"));
        assertEquals("-1", copyNumberMap.getCopyNumberValue("ACAP3", "TCGA-FI-A2F9"));

        assertEquals("0", copyNumberMap.getCopyNumberValue("C1orf159", "1B6EC202-CFF2-4EA1"));
        assertEquals("-1", copyNumberMap.getCopyNumberValue("C1orf159", "TCGA-FI-A2F9"));

        //  Check Nulls
        assertEquals(null, copyNumberMap.getCopyNumberValue("AGRN", "TCGA-FI-A2F9"));
    }
}