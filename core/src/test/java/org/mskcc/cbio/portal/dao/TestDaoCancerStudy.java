/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit Tests for DaoCancer Study.
 *
 * @author Arman Aksoy, Ethan Cerami, Arthur Goldberg.
 */
public class TestDaoCancerStudy extends TestCase {

    /**
     * Tests DaoCancer Study #1.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
    public void testDaoCancerStudy() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        // load cancers
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));
        assertEquals("breast", DaoTypeOfCancer.getTypeOfCancerById("BRCA").getClinicalTrialKeywords());
        assertEquals("colon,rectum,colorectal",
                DaoTypeOfCancer.getTypeOfCancerById("COADREAD").getClinicalTrialKeywords());

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM Description", "gbm","","","brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        //   `CANCER_STUDY_ID` auto_increment counts from 1
        assertEquals(1, cancerStudy.getInternalId());
        
        cancerStudy.setDescription("Glioblastoma");
        DaoCancerStudy.addCancerStudy(cancerStudy,true);
        assertEquals(2, cancerStudy.getInternalId());
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        assertEquals("gbm", cancerStudy.getCancerStudyStableId());
        assertEquals("GBM", cancerStudy.getName());
        assertEquals("Glioblastoma", cancerStudy.getDescription());

        cancerStudy.setName("Breast");
        cancerStudy.setCancerStudyStablId("breast");
        cancerStudy.setDescription("Breast Description");
        DaoCancerStudy.addCancerStudy(cancerStudy);
        assertEquals(3, cancerStudy.getInternalId());

        cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(3);
        assertEquals("Breast Description", cancerStudy.getDescription());
        assertEquals("Breast", cancerStudy.getName());

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(2, list.size());

        assertEquals(null, DaoCancerStudy.getCancerStudyByStableId("no such study"));
        assertTrue(DaoCancerStudy.doesCancerStudyExistByStableId
                (cancerStudy.getCancerStudyStableId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByStableId("no such study"));

        assertTrue(DaoCancerStudy.doesCancerStudyExistByInternalId(cancerStudy.getInternalId()));
        assertFalse(DaoCancerStudy.doesCancerStudyExistByInternalId(-1));

        DaoCancerStudy.deleteCancerStudy(cancerStudy.getInternalId());

        list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(1, list.size());
    }

    /**
     * Tests DaoCancer Study #2.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     */
    public void testDaoCancerStudy2() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        // load cancers
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

        CancerStudy cancerStudy1 = new CancerStudy("GBM public study x", "GBM Description",
                "tcga_gbm1","","", "brca", true);
        DaoCancerStudy.addCancerStudy(cancerStudy1);

        CancerStudy cancerStudy = new CancerStudy("GBM private study x", "GBM Description 2",
                "tcga_gbm2","","", "brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        cancerStudy = new CancerStudy("Breast", "Breast Description",
                "tcga_gbm3","","", "brca", false);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
        assertEquals(3, list.size());

        cancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(1);
        assertEquals(1, cancerStudy1.getInternalId());
        assertEquals("GBM Description", cancerStudy1.getDescription());
        assertEquals("GBM public study x", cancerStudy1.getName());
        assertEquals(true, cancerStudy1.isPublicStudy());

        cancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(2);
        assertEquals(2, cancerStudy1.getInternalId());
        assertEquals("GBM private study x", cancerStudy1.getName());
        assertEquals("GBM Description 2", cancerStudy1.getDescription());
        assertEquals(false, cancerStudy1.isPublicStudy());

        cancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(3);
        assertEquals(3, cancerStudy1.getInternalId());
        assertEquals("Breast", cancerStudy1.getName());
        assertEquals("Breast Description", cancerStudy1.getDescription());
        assertEquals(false, cancerStudy1.isPublicStudy());

        assertEquals(3, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(1);
        assertEquals(2, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteCancerStudy(1);
        assertEquals(2, DaoCancerStudy.getCount());
        DaoCancerStudy.deleteAllRecords();
        assertEquals(0, DaoCancerStudy.getCount());
        assertEquals(null, DaoCancerStudy.getCancerStudyByInternalId(CancerStudy.NO_SUCH_STUDY));
    }
}
