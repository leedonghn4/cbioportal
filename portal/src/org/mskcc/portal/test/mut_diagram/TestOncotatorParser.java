package org.mskcc.portal.test.mut_diagram;

import com.google.common.io.CharStreams;
import junit.framework.TestCase;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorRecord;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorParser;

import java.io.FileReader;

/**
 * Unit test for OncotatorParser.
 */
public class TestOncotatorParser extends TestCase {

    public void testParser() throws Exception {
        FileReader reader = new FileReader("test_data/oncotator0.json");
        String content = CharStreams.toString(reader);
        OncotatorRecord oncotatorRecord = OncotatorParser.parseJSON("key", content);
        assertEquals("PTEN", oncotatorRecord.getGene());
        assertEquals("g.chr10:89653820G>T", oncotatorRecord.getGenomeChange());
        assertEquals("p.E40*", oncotatorRecord.getProteinChange());
        assertEquals("Nonsense_Mutation", oncotatorRecord.getVariantClassification());
    }
}
