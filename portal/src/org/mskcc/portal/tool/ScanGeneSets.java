package org.mskcc.portal.tool;

import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.*;
import org.mskcc.cgds.web_api.GetProfileData;
import org.mskcc.portal.util.ProfileMerger;
import org.mskcc.portal.util.OncoPrintSpecificationDriver;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ProfileDataSummary;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.r_bridge.SurvivalAnalysis;
import org.mskcc.portal.remote.GetClinicalData;
import org.rosuda.REngine.REngineException;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;

/**
 * Scans all Gene Sets.
 */
public class ScanGeneSets {
    private final static String TAB = "\t";

    private static ProfileDataSummary getDataSummary(HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList, ArrayList<CanonicalGene> targetGeneList,
            String caseIds) throws IOException, DaoException {

        String geneListStr = getGenesAsString(targetGeneList);
        ArrayList<String> geneList = getGenesAsStringList(targetGeneList);
        ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver
            (geneListStr, geneticProfileIdSet, profileList, 0);

        OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();

        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        
        for (GeneticProfile geneticProfile:  profileList) {
            GetProfileData remoteCall = new GetProfileData(geneticProfile, geneList, caseIds);
            ProfileData pData = remoteCall.getProfileData();
            profileDataList.add(pData);
        }
        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData mergedProfile = merger.getMergedProfile();
        ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, 0);
        return dataSummary;
    }

    private static String getGenesAsString(ArrayList<CanonicalGene> targetGeneList) {
        StringBuffer buf = new StringBuffer();
        for (CanonicalGene currentGene:  targetGeneList) {
            buf.append (currentGene.getHugoGeneSymbolAllCaps() + " ");
        }
        return buf.toString();
    }

    private static ArrayList<String> getGenesAsStringList(ArrayList<CanonicalGene> targetGeneList) {
        ArrayList<String> geneList = new ArrayList<String>();
        for (CanonicalGene currentGene:  targetGeneList) {
            geneList.add(currentGene.getHugoGeneSymbolAllCaps());
        }
        return geneList;
    }

    private static CaseList getCaseList(CancerStudy cancerStudy, String targetCaseSetId)
        throws DaoException {
        DaoCaseList daoCaseList = new DaoCaseList();
        ArrayList<CaseList> caseList = daoCaseList.getAllCaseLists(cancerStudy.getInternalId());
        for (CaseList currentCaseList:  caseList) {
            String stableId = currentCaseList.getStableId();
            if (targetCaseSetId.equals(stableId)) {
                return currentCaseList;
            }
        }
        return null;
    }

    /**
     * Command Line Util.
     * @param args Command Line Arguments.
     */
    public static void main(String[] args) throws Exception {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        HashSet<String> geneticProfileIdSet = new HashSet<String>();

        // Start of Constants
        geneticProfileIdSet.add("ucec_tcga_mutations");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("ucec_tcga");
        CaseList caseList = getCaseList(cancerStudy, "ucec_endo");
        //CaseList caseList = getCaseList(cancerStudy, "ucec_endo12");
        // End of Constants

        ArrayList <ClinicalData> clinicalDataList =
                GetClinicalData.getClinicalData(new HashSet<String>(caseList.getCaseList()));

        //  Get all Genetic Profiles for Specified Cancer Study
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        ArrayList<GeneticProfile> profileList = daoGeneticProfile.getAllGeneticProfiles(cancerStudy.getInternalId());

        DaoGeneSet daoGeneSet = new DaoGeneSet();
        ArrayList<GeneSet> geneSetList = daoGeneSet.getAllGeneSets();
        ArrayList<Double> osPValueList = new ArrayList<Double>();
        ArrayList<Double> dfsPValueList = new ArrayList<Double>();
        System.out.println ("NAME\tGENES\tFREQUENCY\tOS_P\tDFS_P");
        for (GeneSet currentGeneSet:  geneSetList) {
            ArrayList<CanonicalGene> targetGeneList = currentGeneSet.getGeneList();

            ProfileDataSummary dataSummary = getDataSummary(geneticProfileIdSet,
                    profileList, targetGeneList, caseList.getCaseListAsString());
            if (dataSummary.getNumCasesAffected() > 0) {
                System.out.print (currentGeneSet.getName() + TAB);
                for (CanonicalGene currentGene: targetGeneList) {
                    System.out.print (currentGene + " ");
                }
                System.out.print(TAB);
                System.out.print(dataSummary.getPercentCasesAffected() + TAB);
                try {
                    SurvivalAnalysis survivalAnalysis = new SurvivalAnalysis(clinicalDataList, dataSummary);
                    double osPValue = survivalAnalysis.getOsLogRankPValue();
                    double dfsPValue = survivalAnalysis.getDfsLogRankPValue();
                    osPValueList.add(osPValue);
                    dfsPValueList.add(dfsPValue);
                    System.out.print (osPValue + TAB);
                    System.out.print (dfsPValue);
                } catch (REngineException e) {
                    osPValueList.add(Double.NaN);
                    dfsPValueList.add(Double.NaN);
                    System.out.print (Double.NaN + TAB);
                    System.out.print (Double.NaN);
                }
                System.out.println();
            }
        }

    }
}
