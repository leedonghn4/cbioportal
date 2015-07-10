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

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;

import java.io.*;
import java.util.*;

/**
 * Tests Import of Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class TestImportClinicalData extends TestCase {

	private static final int CANCER_STUDY_ID = 1;

    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
    public void testImportClinicalData() throws Exception {
        ResetDatabase.resetDatabase();
        ProgressMonitor pMonitor = new ProgressMonitor();
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("target/test-classes/clinical_data.txt");
        CancerStudy cancerStudy = new CancerStudy("test","test","test",-1,"test","test",true);
        cancerStudy.setInternalId(CANCER_STUDY_ID);
        ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, clinicalFile);
        importClinicalData.importData();

        LinkedHashSet <String> caseSet = new LinkedHashSet<String>();
        caseSet.add("TCGA-04-1331");
        caseSet.add("TCGA-24-2030");
        caseSet.add("TCGA-24-2261");

        List<Patient> clinicalCaseList = DaoClinicalData.getSurvivalData(1,caseSet);
        assertEquals (3, clinicalCaseList.size());

        Patient clinical0 = clinicalCaseList.get(0);
        assertEquals (new Double(79.04), clinical0.getAgeAtDiagnosis());
        assertEquals ("DECEASED", clinical0.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical0.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(43.8), clinical0.getOverallSurvivalMonths());
        assertEquals (new Double(15.05), clinical0.getDiseaseFreeSurvivalMonths());

        Patient clinical1 = clinicalCaseList.get(1);
        assertEquals (null, clinical1.getAgeAtDiagnosis());
        assertEquals (null, clinical1.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical1.getDiseaseFreeSurvivalStatus());
        assertEquals (null, clinical1.getOverallSurvivalMonths());
        assertEquals (new Double(21.18), clinical1.getDiseaseFreeSurvivalMonths());

        Patient clinical2 = clinicalCaseList.get(2);
        assertEquals (null, clinical2.getDiseaseFreeSurvivalMonths());

		ClinicalParameterMap paramMap = DaoClinicalData.getDataSlice(CANCER_STUDY_ID, Arrays.asList("PLATINUMSTATUS")).get(0);
		assertEquals ("PLATINUMSTATUS", paramMap.getName());
		assertEquals("Sensitive", paramMap.getValue("TCGA-04-1331"));
        assertEquals("MISSING", paramMap.getValue("TCGA-04-1337"));
        assertEquals(4, paramMap.getDistinctCategories().size());

		Set<String> paramSet = DaoClinicalData.getDistinctParameters(CANCER_STUDY_ID);
        assertEquals (9, paramSet.size());
    }
}