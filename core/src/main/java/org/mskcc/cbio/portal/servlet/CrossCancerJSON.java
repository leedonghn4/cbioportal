/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.model.GeneSet;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.GeneSetUtil;
import org.mskcc.cbio.portal.util.XDebug;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * This provides the cross cancer summary page with the
 * case lists of interest. We have separated this because
 * the case list selection is rather complex and should better be
 * handled on the server side.
 */
public class CrossCancerJSON extends HttpServlet {
    private static final Log log = LogFactory.getLog(CrossCancerJSON.class);
    // class which process access control to cancer studies
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     *
     * @throws javax.servlet.ServletException Serlvet Init Error.
     */
    public void init() throws ServletException {
        super.init();
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
        accessControl = (AccessControl)context.getBean("accessControl");
    }

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  Http Servlet Request Object.
     * @param httpServletResponse Http Servelt Response Object.
     * @throws javax.servlet.ServletException Servlet Error.
     * @throws java.io.IOException            IO Error.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        //  Cancer All Cancer Studies
        try {
            // Get priority settings
            //  Get priority settings
            Integer dataTypePriority;
            try {
                String priority = httpServletRequest.getParameter(QueryBuilder.DATA_PRIORITY);
                if(priority != null) {
                    dataTypePriority = Integer.parseInt(priority.trim());
                } else
                    dataTypePriority = 0;
            } catch (NumberFormatException e) {
                dataTypePriority = 0;
            }
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();

            JSONArray rootMap =  new JSONArray();
            for (CancerStudy cancerStudy : cancerStudiesList) {
                if(cancerStudy.getCancerStudyStableId().equals("all")) continue;

                String stableId = cancerStudy.getCancerStudyStableId();
                ArrayList<GeneticProfile> geneticProfiles = GetGeneticProfiles.getGeneticProfiles(stableId);

                //  Get the default case set
                ArrayList<CaseList> caseSetList = GetCaseSets.getCaseSets(stableId);
                AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList, dataTypePriority);
                CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();

                //  Get the default genomic profiles
                CategorizedGeneticProfileSet categorizedGeneticProfileSet
                        = new CategorizedGeneticProfileSet(geneticProfiles);
                HashMap<String, GeneticProfile> defaultGeneticProfileSet = null;
                switch (dataTypePriority) {
                    case 2:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultCopyNumberMap();
                        break;
                    case 1:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationMap();
                        break;
                    case 0:
                    default:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
                }

                String profilesStr = "";
                for (String profile : defaultGeneticProfileSet.keySet()) {
                    profilesStr += profile + " ";
                }
                if(!profilesStr.isEmpty()) profilesStr = profilesStr.substring(0, profilesStr.length()-1);
                else profilesStr = null;

                String cases = "";
                if(defaultCaseSet == null) {
                    cases = null;
                } else {
                    for (String aCase : defaultCaseSet.getCaseList()) {
                        cases += aCase + " ";
                    }
                    if(!cases.isEmpty()) cases = cases.substring(0, cases.length()-1);
                }

                Map jsonCancerStudySubMap = new LinkedHashMap();
                jsonCancerStudySubMap.put("id", stableId);
                jsonCancerStudySubMap.put("name", cancerStudy.getName());
                jsonCancerStudySubMap.put("description", cancerStudy.getDescription());
                jsonCancerStudySubMap.put("citation", cancerStudy.getCitation());
                jsonCancerStudySubMap.put("pmid", cancerStudy.getPmid());
                jsonCancerStudySubMap.put("has_mutation_data", cancerStudy.hasMutationData(geneticProfiles));
                jsonCancerStudySubMap.put("has_mutsig_data", cancerStudy.hasMutSigData());
                jsonCancerStudySubMap.put("has_gistic_data", cancerStudy.hasGisticData());
                jsonCancerStudySubMap.put("genetic_profiles", profilesStr);
                jsonCancerStudySubMap.put("case_set", cases);
                rootMap.add(jsonCancerStudySubMap);
            }

            httpServletResponse.setContentType("application/json");
            String jsonText = JSONValue.toJSONString(rootMap);
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write(jsonText);
            writer.flush();
            writer.close();
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
        }
    }

}
