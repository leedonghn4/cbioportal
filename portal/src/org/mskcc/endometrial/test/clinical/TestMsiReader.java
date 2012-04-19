package org.mskcc.endometrial.test.clinical;

import junit.framework.TestCase;
import org.mskcc.endometrial.clinical.MsiReader;

import java.io.File;
import java.io.IOException;

/**
 * Tests the MSI Reader Utility Class.
 */
public class TestMsiReader extends TestCase {

    public void testPrepareClinicalFile() throws IOException {
        File msiFile = new File ("test_data/msi_test.txt");
        MsiReader msiReader = new MsiReader(msiFile);
        assertEquals("MSS", msiReader.getMsi5Status("TCGA-A5-A0G3"));
        assertEquals("MSS", msiReader.getMsi7Status("TCGA-A5-A0G3"));
        assertEquals("MSI-H", msiReader.getMsi5Status("TCGA-A5-A0GB"));
        assertEquals("MSI-H", msiReader.getMsi7Status("TCGA-A5-A0GB"));
    }
}
