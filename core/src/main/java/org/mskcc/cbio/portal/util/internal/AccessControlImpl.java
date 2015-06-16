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

package org.mskcc.cbio.portal.util.internal;

// imports
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import org.apache.commons.logging.*;

import java.util.*;

/**
 * Utilities for managing access control.
 *
 * @author Benjamin Gross
 */
public class AccessControlImpl implements AccessControl {

	// ref to log
	private static Log log = LogFactory.getLog(AccessControlImpl.class);

    /**
     * Gets Cancer Studies. Used by QueryBuilder.
     *
     * @return List<CancerStudy>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
	 *
	 * We use @PostFilter annotation to remove elements
	 * in the return list inaccessible to the user.
     */
    public List<CancerStudy> getCancerStudies() throws DaoException, ProtocolException {

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), getting accessible cancer studies.");
		}

		// get list of accessible cancer studies
        List<CancerStudy> accessibleCancerStudies = DaoCancerStudy.getAllCancerStudies();

        if (accessibleCancerStudies.size() > 0) {

            //  sort the list
            Collections.sort(accessibleCancerStudies, new CancerStudiesComparator());

            //  Then, insert "All" Cancer Types at beginning
            ArrayList<CancerStudy> finalCancerStudiesList = new ArrayList<CancerStudy>();
			String allCancerStudyTitle = (GlobalProperties.usersMustBeAuthorized()) ?
				"All Authorized Cancer Studies" : "All Cancer Studies";
            CancerStudy cancerStudy = new CancerStudy(allCancerStudyTitle, allCancerStudyTitle,"","",
                                                      "all", "all", true);
            finalCancerStudiesList.add(cancerStudy);
            finalCancerStudiesList.addAll(accessibleCancerStudies);
            
            return finalCancerStudiesList;
        } else {
            throw new ProtocolException("No cancer studies accessible; "+
                                        "either provide credentials to access private studies, " +
                                        "or ask administrator to load public ones.\n");
        }
    }

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return List<CancerStudy>
     * @throws DaoException
	 *
	 * We use @PostFilter rather than @PreAuthorize annotation to provide 
	 * permission evaluation on this cancer study so that we can process
	 * invalid permissions via QueryBuilder.validateForm().  If we use @PreAuthorize,
	 * thread execution does not return from this method call if a user has invalid permissions.
     */
    public List<CancerStudy> isAccessibleCancerStudy(String stableStudyId) throws DaoException {

		if (log.isDebugEnabled()) {
			log.debug("hasPermission(), stableStudyId: " + stableStudyId);
		}

		// get cancer study by stable id
		List<CancerStudy> toReturn = new ArrayList<CancerStudy>();
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(stableStudyId);
		if (cancerStudy != null) {
			toReturn.add(cancerStudy);
		}
		
		// outta here
		return toReturn;
	}
}

/**
 * Compares Cancer Studies, so that we can sort them alphabetically.
 */
class CancerStudiesComparator implements Comparator {

    /**
     * Compare two cancer studies.
     * @param o  First Cancer Study.
     * @param o1 Second Cancer Study.
     * @return int indicating name sort order.
     */
    public int compare(Object o, Object o1) {
        CancerStudy study0 = (CancerStudy) o;
        CancerStudy study1 = (CancerStudy) o1;
        return study0.getName().compareTo(study1.getName());
    }
}