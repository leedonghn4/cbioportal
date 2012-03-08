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
        assertEquals("p.?(4)|p.Y27_N212>Y(2)|p.Y27fs*1(2)|p.E40*(1)", oncotatorRecord.getCosmicOverlappingMutations());
        assertEquals(4, oncotatorRecord.getCosmicRecords().size());
        assertEquals("p.?", oncotatorRecord.getCosmicRecords().get(0).getProteinChange());
        assertEquals(4, oncotatorRecord.getCosmicRecords().get(0).getNumRecords());
        assertEquals("p.E40*", oncotatorRecord.getCosmicRecords().get(3).getProteinChange());
        assertEquals(1, oncotatorRecord.getCosmicRecords().get(3).getNumRecords());
        assertEquals(1, oncotatorRecord.getNumExtactCosmicRecords());
    }
}