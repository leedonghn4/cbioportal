package org.mskcc.endometrial.clinical;

import org.mskcc.endometrial.cna.CnaClusterReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;

/**
 * Maintains Categorized List of Cases.
 */
public class CaseListUtil {
    private static final String TAB = "\t";
    private HashSet<String> endoGrade1Set = new HashSet<String>();
    private HashSet<String> endoGrade2Set = new HashSet<String>();
    private HashSet<String> endoGrade3Set = new HashSet<String>();
    private HashSet<String> serousSet = new HashSet<String>();
    private HashSet<String> mixedSet = new HashSet<String>();

    /**
     * Default Constructor.
     */
    public CaseListUtil() {
    }

    /**
     * Categorize Cases by Histological Subtype.
     * @param histSubType   Histological Subtype.
     * @param caseId        Case ID.
     */
    public void categorizeByHistologicalSubType(String histSubType, String caseId) {
        if (histSubType.equals("Endometrioid endometrial adenocarcinoma (Grade 1)")) {
            endoGrade1Set.add(caseId);
        } else if(histSubType.equals("Endometrioid endometrial adenocarcinoma (Grade 2)")) {
            endoGrade2Set.add(caseId);
        } else if (histSubType.equals("Endometrioid endometrial adenocarcinoma (Grade 3)")) {
            endoGrade3Set.add(caseId);
        } else if (histSubType.equals("Uterine serous endometrial adenocarcinoma")) {
            serousSet.add(caseId);
        } else if (histSubType.equals("Mixed serous and endometrioid")) {
            mixedSet.add(caseId);
        } else if (histSubType.equals("[Discrepancy]")) {
            //  Do nothing.  ignore.
        } else {
            throw new IllegalArgumentException ("Aborting.  Unknown Histological Subtype:  " + histSubType);
        }
    }

    public void writeCaseLists(HashSet<String> sequencedCaseSet, CnaClusterReader cnaClusterReader,
           String outputDir) throws IOException {
        outputHistologicalSubtypes(sequencedCaseSet, outputDir);
        outputAllEndometriodCases(sequencedCaseSet, outputDir);
        outputCNAClusters(sequencedCaseSet, cnaClusterReader, outputDir);
    }

    private void outputCNAClusters(HashSet<String> sequencedCaseSet, CnaClusterReader cnaClusterReader,
                                   String outputDir) throws IOException {
        HashSet<String> cluster1Set = cnaClusterReader.getCluster1Set();
        HashSet<String> cluster2Set = cnaClusterReader.getCluster2Set();
        HashSet<String> cluster3Set = cnaClusterReader.getCluster3Set();
        outputCaseSet(cluster1Set, sequencedCaseSet, "ucec_tcga_cna_cluster_1_sequenced",
                "CNA Cluster 1 - Sequenced",
                "CNA Cluster 1 - Endometrioids with very few or no SNCA (Sequenced Cases Only)", true, outputDir);
        outputCaseSet(cluster2Set, sequencedCaseSet, "ucec_tcga_cna_cluster_2_sequenced",
                "CNA Cluster 2 - Sequenced",
                "CNA Cluster 2 - Endometrioids with some SNCA (Sequenced Cases Only)", true, outputDir);
        outputCaseSet(cluster3Set, sequencedCaseSet, "ucec_tcga_cna_cluster_3_sequenced",
                "CNA Cluster 3 - Sequenced",
                "CNA Cluster 3 - Serous Like (Sequenced Cases Only)", true, outputDir);
    }

    private void outputAllEndometriodCases(HashSet<String> sequencedCaseSet, String outputDir) throws IOException {
        HashSet<String> allEndoSet = new HashSet<String>();
        allEndoSet.addAll(endoGrade1Set);
        allEndoSet.addAll(endoGrade2Set);
        allEndoSet.addAll(endoGrade3Set);
        outputCaseSet(allEndoSet, sequencedCaseSet, "ucec_tcga_endo_all",
                "Subtype:  Endometriod:  Grades 1-3 - All",false, outputDir);
        outputCaseSet(allEndoSet, sequencedCaseSet, "ucec_tcga_endo_sequenced",
                "Subtype:  Endometriod:  Grades 1-3 - Sequenced", true, outputDir);
    }

    private void outputHistologicalSubtypes(HashSet<String> sequencedCaseSet, String outputDir) throws IOException {
        outputCaseSet(endoGrade1Set, sequencedCaseSet, "ucec_tcga_endo_grade1_all",
                "Subtype:  Endometriod:  Grade 1 - All",false, outputDir);
        outputCaseSet(endoGrade1Set, sequencedCaseSet, "ucec_tcga_endo_grade1_sequenced",
                "Subtype:  Endometriod:  Grade 1 - Sequenced", true, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, "ucec_tcga_endo_grade2_all",
                "Subtype:  Endometriod:  Grade 2 - All", false, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, "ucec_tcga_endo_grade2_sequenced",
                "Subtype:  Endometriod:  Grade 2 - Sequenced", true, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, "ucec_tcga_endo_grade3_all",
                "Subtype:  Endometriod:  Grade 3 - All", false, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, "ucec_tcga_endo_grade3_sequenced",
                "Subtype:  Endometriod:  Grade 3 - Sequenced", true, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, "ucec_tcga_serous_all",
                "Subtype:  Endometriod:  Serous - All", false, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, "ucec_tcga_serous_sequenced",
                "Subtype:  Serous - Sequenced", true, outputDir);

        outputCaseSet(mixedSet, sequencedCaseSet, "ucec_tcga_mixed_all",
                "Subtype:  Mixed Serous and Endometriod - All", false, outputDir);
        outputCaseSet(mixedSet, sequencedCaseSet, "ucec_tcga_mixed_sequenced",
                "Subtype:  Mixed Serous and Endometriod - Sequenced", true, outputDir);
    }

    private void outputCaseSet(HashSet<String> caseSet, HashSet<String> sequencedCaseSet,
        String stableId, String name, String description, boolean onlyIncludeSequencedCases,
        String outputDir) throws IOException {
        StringBuffer caseIds = new StringBuffer();
        int sampleCount = 0;
        for (String caseId:  caseSet) {
            if (onlyIncludeSequencedCases) {
                if (sequencedCaseSet.contains(caseId)) {
                    caseIds.append(caseId + TAB);
                    sampleCount++;
                }
            } else {
                caseIds.append(caseId + TAB);
                sampleCount++;
            }
        }

        name = name + " [" + sampleCount + " samples]";
        description = description + " [Auto generated on " + new Date() + "].";
        File outputFile = new File(outputDir + "/case_lists/" + stableId + ".txt");
        System.out.println ("Writing case set:  " + outputFile.getAbsolutePath());
        writeFile(stableId, name, description, caseIds, outputFile);
    }

    private void writeFile(String stableId, String name, String description, StringBuffer caseIds,
            File outputFile) throws IOException {
        FileWriter outWriter = new FileWriter(outputFile);
        outWriter.write("cancer_study_identifier: ucec_tcga\n");
        outWriter.write("stable_id: " + stableId + "\n");
        outWriter.write("case_list_name: " + name + "\n");
        outWriter.write("case_list_description: " + description + "\n");
        outWriter.write("case_list_ids:  " + caseIds + "\n");
        outWriter.flush();
        outWriter.close();
    }

    private void outputCaseSet(HashSet<String> caseSet, HashSet<String> sequencedCaseSet,
        String stableId, String name, boolean onlyIncludeSequencedCases,
        String outputDir) throws IOException {
        outputCaseSet(caseSet, sequencedCaseSet, stableId, name, name, onlyIncludeSequencedCases,
                outputDir);
    }    
}