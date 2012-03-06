package org.mskcc.endometrial.test.methylation;

import junit.framework.TestCase;
import org.mskcc.endometrial.methylation.MethylationReader;

import java.io.File;
import java.io.IOException;

/**
 * Tests the Methylation Reader.
 */
public class TestMethylationReader extends TestCase {

    public void testMethylationReader() throws IOException {
        File mlh1MethFile = new File ("test_data/mlh1_meth_test.txt");
        MethylationReader reader = new MethylationReader(mlh1MethFile);
        assertEquals ("1", reader.getMethylationStatus("TCGA-A5-A0VO"));
        assertEquals ("0", reader.getMethylationStatus("TCGA-AX-A0J0"));
    }
}
