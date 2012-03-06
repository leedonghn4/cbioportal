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
        assertEquals("MSI-H", msiReader.getMsiStatus("TCGA-AP-A051"));
        assertEquals("MSI-H", msiReader.getMsiStatus("TCGA-D1-A1NZ"));
    }
}
