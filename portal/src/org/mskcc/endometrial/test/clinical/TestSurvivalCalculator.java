package org.mskcc.endometrial.test.clinical;

import junit.framework.TestCase;
import org.mskcc.endometrial.clinical.SurvivalCalculator;

import java.io.IOException;

/**
 * Tests the Survival Calculator.
 */
public class TestSurvivalCalculator extends TestCase {

    public void testSurvivalCalculator() throws IOException {
        assertEquals("47.54", SurvivalCalculator.calculateOsMonths("LIVING", "1447", "1447", "[Not Available]"));
        assertEquals ("0.39", SurvivalCalculator.calculateDfsMonths("Yes", "12", "1447"));

        assertEquals("6.21", SurvivalCalculator.calculateOsMonths("DECEASED", "189", "[Not Available]", "189"));
        assertEquals("3.12", SurvivalCalculator.calculateDfsMonths("Yes", "95", "189"));

        assertEquals("17.05", SurvivalCalculator.calculateOsMonths("LIVING", "519", "407", "[Not Available]"));
        assertEquals("NA", SurvivalCalculator.calculateDfsMonths("Yes", "", "679"));

        assertEquals("10.55", SurvivalCalculator.calculateDfsMonths("Yes", "321", "519"));
    }
}
