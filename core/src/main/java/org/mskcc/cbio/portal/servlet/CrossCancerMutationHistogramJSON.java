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

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.*;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.OncoPrintSpecificationDriver;
import org.mskcc.cbio.portal.util.ProfileMerger;
import org.mskcc.cbio.portal.util.ZScoreUtil;
import org.owasp.validator.html.PolicyException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class CrossCancerMutationHistogramJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CrossCancerMutationHistogramJSON.class);
    private ServletXssUtil servletXssUtil;
    private AccessControl accessControl;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
            ApplicationContext context =
                    new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
            accessControl = (AccessControl)context.getBean("accessControl");
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONArray table = new JSONArray();

        String gene = request.getParameter("gene");
        String mutation = request.getParameter("mutation");
        Integer dataTypePriority = 0;
        String dpParameter = request.getParameter(QueryBuilder.DATA_PRIORITY);

        if(dpParameter != null) {
            try {
                dataTypePriority
                        = Integer.parseInt(dpParameter.trim());
            } catch (NumberFormatException e) {
                dataTypePriority = 0;
            }
        }
        request.setAttribute(QueryBuilder.DATA_PRIORITY, dataTypePriority);

        JSONArray header = new JSONArray();
        header.add("Cancer Study");
        header.add("Number of mutations");
        table.add(header);

        if(gene != null && mutation != null) {
            gene = gene.trim();
            mutation = mutation.trim();

            try {
                for (CancerStudy cancerStudy : getCancerStudiesWithData()) {
                    String cancerStudyId = cancerStudy.getCancerStudyStableId();

                    // First the case set
                    //  Get all Genetic Profiles Associated with this Cancer Study ID.
                    ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);
                    //  Get all Case Lists Associated with this Cancer Study ID.
                    ArrayList<CaseList> caseSetList = GetCaseSets.getCaseSets(cancerStudyId);

                    //  Get the default case set
                    AnnotatedCaseSets annotatedCaseSets = new AnnotatedCaseSets(caseSetList, dataTypePriority);
                    CaseList defaultCaseSet = annotatedCaseSets.getDefaultCaseList();
                    if(defaultCaseSet == null) continue;

                    //  Get the default genomic profiles
                    CategorizedGeneticProfileSet categorizedGeneticProfileSet =
                            new CategorizedGeneticProfileSet(geneticProfileList);
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

                    // Second the mutations
                    Iterator<String> profileIterator = defaultGeneticProfileSet.keySet().iterator();
                    String caseIds = defaultCaseSet.getCaseListAsString();
                    ArrayList<GeneticProfile> profileList
                            = new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values());

                    String query = gene + ": MUT = " + mutation;
                    HashSet<String> geneticProfileIdSet = new HashSet<String>(defaultGeneticProfileSet.keySet());
                    // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
                    double zScore = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
                    double rppaScore = ZScoreUtil.getRPPAScore(request);

                    OncoPrintSpecification specification
                            = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(
                                query,
                                geneticProfileIdSet,
                                profileList,
                                zScore,
                                rppaScore
                            ).getTheOncoPrintSpecification();

                    ArrayList<String> genes = new ArrayList<String>();
                    genes.add(gene);

                    ArrayList<ProfileData> profileDatas = new ArrayList<ProfileData>();
                    for (GeneticProfile geneticProfile : defaultGeneticProfileSet.values()) {
                        GetProfileData remoteCall = new GetProfileData(geneticProfile, genes, caseIds);
                        ProfileData pData = remoteCall.getProfileData();
                        profileDatas.add(pData);
                    }

                    ProfileMerger merger = new ProfileMerger(profileDatas);
                    ProfileData mergedProfile = merger.getMergedProfile();

                    ProfileDataSummary dataSummary
                            = new ProfileDataSummary(mergedProfile, specification, zScore, rppaScore);

                    JSONArray studyData = new JSONArray();
                    studyData.add(cancerStudy.getName());
                    studyData.add(dataSummary.getNumCasesAffected());
                    table.add(studyData);
                }
            } catch (DaoException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            JSONValue.writeJSONString(table, out);
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Google Column Charts compatible data in JSON format ";
    }

    private ArrayList<CancerStudy> getCancerStudiesWithData() throws DaoException, ProtocolException {
        List<CancerStudy> candidateCancerStudyList = accessControl.getCancerStudies();
        ArrayList<CancerStudy> finalCancerStudyList = new ArrayList<CancerStudy>();

        //  Only include cancer studies that have default CNA and/or default mutation
        for (CancerStudy currentCancerStudy : candidateCancerStudyList) {
            if (hasDefaultCnaOrMutationProfiles(currentCancerStudy)) {
                finalCancerStudyList.add(currentCancerStudy);
            }
        }
        return finalCancerStudyList;
    }

    private boolean hasDefaultCnaOrMutationProfiles(CancerStudy currentCancerStudy) throws DaoException {
        ArrayList<GeneticProfile> geneticProfileList
                = GetGeneticProfiles.getGeneticProfiles(currentCancerStudy.getCancerStudyStableId());
        CategorizedGeneticProfileSet categorizedSet = new CategorizedGeneticProfileSet(geneticProfileList);
        return categorizedSet.getNumDefaultMutationAndCopyNumberProfiles() > 0;
    }
}
