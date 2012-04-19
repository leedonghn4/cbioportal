package org.mskcc.endometrial.test.cluster;

import junit.framework.TestCase;
import org.mskcc.endometrial.cluster.ClusterReader;
import org.mskcc.endometrial.methylation.MethylationReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests the Cluster Reader.
 */
public class TestClusterReader extends TestCase {

    public void testClusterReader() throws IOException {
        File file = new File ("test_data/micro_rna_clusters.txt");
        ClusterReader reader = new ClusterReader(file);
        ArrayList<String> headerList = reader.getHeaderList();
        assertEquals(3, headerList.size());
        assertEquals("micro_rna_cluster", headerList.get(0));
        assertEquals("micro_rna_score", headerList.get(1));
        assertEquals("index", headerList.get(2));

        ArrayList<String> valueList = reader.getValueList("TCGA-BG-A18B");
        assertEquals(3, valueList.size());
        assertEquals("1", valueList.get(0));
        assertEquals("0.807377395", valueList.get(1));
        assertEquals("12", valueList.get(2));
    }
}
