/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.web_api.GetProfileData;
import org.mskcc.cbio.portal.model.DownloadLink;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.cgds.validate.gene.GeneValidator;
import org.mskcc.cbio.cgds.validate.gene.GeneValidationException;
import org.mskcc.cbio.portal.remote.GetMutationData;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.owasp.validator.html.PolicyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Central Servlet for performing Cross-Cancer Study Queries.
 *
 * @author Ethan Cerami.
 */
public class CrossCancerStudyServlet extends HttpServlet {

    private ServletXssUtil servletXssUtil;

	// class which process access control to cancer studies
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
     * Handles HTTP GET Request.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles HTTP POST Request.
     */
    protected void doPost(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException,
            IOException {
        XDebug xdebug = new XDebug();
        xdebug.startTimer();
        try {
            String geneListStr = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.GENE_LIST);
            ArrayList<CancerStudy> cancerStudyList = getCancerStudiesWithData();
            ArrayList<CancerStudy> ccStudiesOfInterest = new ArrayList<CancerStudy>();
            ArrayList<CaseList> caseListList = new ArrayList<CaseList>();
            ArrayList<HashMap<String, GeneticProfile>> geneticProfiles
                    = new ArrayList<HashMap<String, GeneticProfile>>();
            ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();
            ArrayList<ProfileData> profileDatas = new ArrayList<ProfileData>();
            ArrayList<String> geneList = null;

            Integer dataTypePriority;
            try {
                dataTypePriority
                        = Integer.parseInt(httpServletRequest.getParameter(QueryBuilder.DATA_PRIORITY).trim());
            } catch (NumberFormatException e) {
                dataTypePriority = 0;
            }
            httpServletRequest.setAttribute(QueryBuilder.DATA_PRIORITY, dataTypePriority);

            for (CancerStudy cancerStudy : cancerStudyList) {
                if(dataTypePriority < 2 && cancerStudy.hasMutationData()) {
                    ccStudiesOfInterest.add(cancerStudy);
                } else if(cancerStudy.hasCnaData()) {
                    ccStudiesOfInterest.add(cancerStudy);
                }
            }

            // This is where we collect the mutations and the case sets
            for (CancerStudy cancerStudy : ccStudiesOfInterest) {
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
                caseListList.add(defaultCaseSet);

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
                geneticProfiles.add(defaultGeneticProfileSet);

                // Second the mutations
                Iterator<String> profileIterator = defaultGeneticProfileSet.keySet().iterator();
                String caseIds = defaultCaseSet.getCaseListAsString();
                ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>(defaultGeneticProfileSet.values());


                HashSet<String> geneticProfileIdSet = new HashSet<String>(defaultGeneticProfileSet.keySet());
                // parse geneList, written in the OncoPrintSpec language (except for changes by XSS clean)
                double zScore = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, httpServletRequest);
                double rppaScore = ZScoreUtil.getRPPAScore(httpServletRequest);

                ParserOutput theOncoPrintSpecParserOutput =
                        OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(geneListStr,
                                geneticProfileIdSet, profileList, zScore, rppaScore);

                geneList = new ArrayList<String>();
                geneList.addAll( theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes());
                ArrayList<String> tempGeneList = new ArrayList<String>();
                for (String gene : geneList){
                    tempGeneList.add(gene);
                }
                geneList = tempGeneList;

                ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
                while (profileIterator.hasNext()) {
                    String profileId = profileIterator.next();
                    GeneticProfile profile = defaultGeneticProfileSet.get(profileId);
                    if( null == profile ){
                        continue;
                    }
                    xdebug.logMsg(this, "Getting data for:  " + profile.getProfileName());
                    GetProfileData remoteCall = new GetProfileData(profile, geneList, caseIds);
                    ProfileData pData = remoteCall.getProfileData();
                    if( pData == null ){
                        System.err.println( "pData == null" );
                    }else{
                        if( pData.getGeneList() == null ){
                            System.err.println( "pData.getValidGeneList() == null" );
                        }
                    }
                    if (pData != null) {
                        xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                        xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
                        profileDataList.add(pData);
                    }
                    xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());

                    //  Optionally, get Extended Mutation Data.
                    if (profile.getGeneticAlterationType().equals
                            (GeneticAlterationType.MUTATION_EXTENDED)) {
                        if (geneList.size() <= QueryBuilder.MUTATION_DETAIL_LIMIT) {
                            xdebug.logMsg(this, "Number genes requested is <= " + QueryBuilder.MUTATION_DETAIL_LIMIT);
                            xdebug.logMsg(this, "Therefore, getting extended mutation data");
                            GetMutationData remoteCallMutation = new GetMutationData();
                            ArrayList<ExtendedMutation> tempMutationList =
                                    remoteCallMutation.getMutationData(profile,
                                            geneList, new HashSet<String>(defaultCaseSet.getCaseList()), xdebug);
                            if (tempMutationList != null && tempMutationList.size() > 0) {
                                xdebug.logMsg(this, "Total number of mutation records retrieved:  "
                                        + tempMutationList.size());
                                mutationList.addAll(tempMutationList);
                            }
                        } else {
                            httpServletRequest.setAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED, Boolean.TRUE);
                        }
                    }
                }

                xdebug.logMsg(this, "Merging Profile Data");
                ProfileMerger merger = new ProfileMerger(profileDataList);
                ProfileData mergedProfile = merger.getMergedProfile();
                profileDatas.add(mergedProfile);

                xdebug.logMsg(this, "Merged Profile, Number of genes:  "
                        + mergedProfile.getGeneList().size());
                xdebug.logMsg(this, "Merged Profile, Number of cases:  "
                        + mergedProfile.getCaseIdList().size());



            } // end of a single cancer study analysis

            // Pass all inferred values to the request for later use in cross cancer jsp
            httpServletRequest.setAttribute(QueryBuilder.CROSS_CANCER_STUDIES, ccStudiesOfInterest);
            httpServletRequest.setAttribute(QueryBuilder.CROSS_CANCER_CASESETS, caseListList);
            httpServletRequest.setAttribute(QueryBuilder.CROSS_CANCER_PROFILES, geneticProfiles);
            httpServletRequest.setAttribute(QueryBuilder.CROSS_CANCER_MERGED_PROFILE_DATAS, profileDatas);
            httpServletRequest.setAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST, mutationList);
            httpServletRequest.setAttribute(QueryBuilder.CROSS_CANCER_GENES, geneList);

            httpServletRequest.setAttribute(QueryBuilder.CANCER_STUDY_ID, AccessControl.ALL_CANCER_STUDIES_ID);
            httpServletRequest.setAttribute(QueryBuilder.CANCER_TYPES_INTERNAL, cancerStudyList);
            httpServletRequest.setAttribute(QueryBuilder.XDEBUG_OBJECT, xdebug);

            String action = servletXssUtil.getCleanInput(httpServletRequest, QueryBuilder.ACTION_NAME);
            if (action != null && action.equals(QueryBuilder.ACTION_SUBMIT)) {
                new GeneValidator(geneListStr);
                dispatchToResultsJSP(httpServletRequest, httpServletResponse);
            } else {
                dispatchToIndexJSP(httpServletRequest, httpServletResponse);
            }
        } catch (GeneValidationException e) {
            httpServletRequest.setAttribute(QueryBuilder.STEP4_ERROR_MSG, e.getMessage());
            dispatchToIndexJSP(httpServletRequest, httpServletResponse);
        } catch (DaoException e) {
            throw new ServletException(e);
        } catch (ProtocolException e) {
            throw new ServletException(e);
		}
    }

    private void dispatchToResultsJSP(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/cross_cancer_results.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private void dispatchToIndexJSP(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws ServletException, IOException {
        RequestDispatcher dispatcher =
                getServletContext().getRequestDispatcher("/WEB-INF/jsp/index.jsp");
        dispatcher.forward(httpServletRequest, httpServletResponse);
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

    private boolean hasDefaultCnaOrMutationProfiles(CancerStudy currentCancerStudy)
            throws DaoException {
        ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles
                (currentCancerStudy.getCancerStudyStableId());
        CategorizedGeneticProfileSet categorizedSet =
                new CategorizedGeneticProfileSet(geneticProfileList);
        return categorizedSet.getNumDefaultMutationAndCopyNumberProfiles() > 0;
    }
}