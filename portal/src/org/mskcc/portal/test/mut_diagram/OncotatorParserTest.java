package org.mskcc.portal.test.mut_diagram;

import com.google.common.io.CharStreams;
import junit.framework.TestCase;
import org.mskcc.portal.mut_diagram.oncotator.Oncotator;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorParser;

import java.io.FileReader;

/**
 * Unit test for OncotatorParser.
 */
public class OncotatorParserTest extends TestCase {

    public void testParser() throws Exception {
        FileReader reader = new FileReader("test_data/oncotator0.json");
        String content = CharStreams.toString(reader);
        Oncotator oncotator = OncotatorParser.parseJSON(content);
        assertEquals("PTEN", oncotator.getGene());
        assertEquals("g.chr10:89653820G>T", oncotator.getGenomeChange());
        assertEquals("p.E40*", oncotator.getProteinChange());
        assertEquals("Nonsense_Mutation", oncotator.getVariantClassification());
    }
}
