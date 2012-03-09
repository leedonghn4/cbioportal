package org.mskcc.endometrial.test.rppa;

import junit.framework.TestCase;
import org.mskcc.endometrial.cna.CopyNumberMap;
import org.mskcc.endometrial.rppa.RppaReader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Tests the RPPA Reader Class.
 */
public class TestRppaReader extends TestCase {

    public void testCopyNumberMap() throws IOException {
        File rppaFile = new File ("test_data/test_rppa.txt");
        RppaReader reader = new RppaReader(rppaFile);
        assertEquals("-0.823288713", reader.get_AKT_pS473("TCGA-A5-A0R7"));
        assertEquals("-0.192983932", reader.get_AKT_pT308("TCGA-A5-A0R7"));
        assertEquals("-0.424352713", reader.get_PTEN_PROTEIN_LEVEL("TCGA-A5-A0R7"));

        assertEquals("-0.162906641", reader.get_AKT_pS473("TCGA-BG-A18C"));
        assertEquals("-0.142519895", reader.get_AKT_pT308("TCGA-BG-A18C"));
        assertEquals("0.122624914", reader.get_PTEN_PROTEIN_LEVEL("TCGA-BG-A18C"));
    }
}