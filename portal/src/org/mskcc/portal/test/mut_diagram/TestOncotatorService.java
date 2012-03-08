package org.mskcc.portal.test.mut_diagram;

import junit.framework.TestCase;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorRecord;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorService;
import org.mskcc.cgds.dao.DaoOncotatorCache;

/**
 * Unit test for OncotatorServer.
 */
public class TestOncotatorService extends TestCase {

    public void testOncotator() throws Exception {
        DaoOncotatorCache.deleteAllRecords();
        OncotatorService service = OncotatorService.getInstance();
        OncotatorRecord record = service.getOncotatorAnnotation("7",55259515, 55259515, "T", "G");
        assertEquals("EGFR", record.getGene());
        assertEquals("g.chr7:55259515T>G", record.getGenomeChange());
        assertEquals("p.L858R", record.getProteinChange());
        assertEquals("Missense_Mutation", record.getVariantClassification());
    }
}