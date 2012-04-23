package org.mskcc.endometrial.clinical;

import org.mskcc.endometrial.rnaseq.RnaSeqReader;

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
        if (histSubType.equals("EndoGr1")) {
            endoGrade1Set.add(caseId);
        } else if(histSubType.equals("EndoGr2")) {
            endoGrade2Set.add(caseId);
        } else if (histSubType.equals("EndoGr3")) {
            endoGrade3Set.add(caseId);
        } else if (histSubType.equals("SerousGr3")) {
            serousSet.add(caseId);
        } else if (histSubType.equals("MixedGr3")) {
            mixedSet.add(caseId);
        } else if (histSubType.equals("Normal")) {
            // do nothing...
        } else {
            throw new IllegalArgumentException ("Aborting.  Unknown Histological Subtype:  " + histSubType);
        }
    }

    public void writeCaseLists(HashSet<String> sequencedCaseSet, HashSet<String> gisticCaseSet,
               RnaSeqReader rnaSeqReader, String outputDir) throws IOException {
        outputHistologicalSubtypes(sequencedCaseSet, gisticCaseSet, rnaSeqReader, outputDir);
        outputAllEndometriodCases(sequencedCaseSet, gisticCaseSet, rnaSeqReader, outputDir);
    }

    private void outputAllEndometriodCases(HashSet<String> sequencedCaseSet, HashSet<String> gisticCaseSet,
               RnaSeqReader rnaSeqReader, String outputDir) throws IOException {
        HashSet<String> allEndoSet = new HashSet<String>();
        allEndoSet.addAll(endoGrade1Set);
        allEndoSet.addAll(endoGrade2Set);
        allEndoSet.addAll(endoGrade3Set);
        outputCaseSet(allEndoSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_all",
                "Subtype:  Endometriod:  Grades 1-3 - All",false, outputDir);
        outputCaseSet(allEndoSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_core",
                "Subtype:  Endometriod:  Grades 1-3 - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);
    }

    private void outputHistologicalSubtypes(HashSet<String> sequencedCaseSet, HashSet<String> gisticCaseSet,
        RnaSeqReader rnaSeqReader, String outputDir) throws IOException {
        outputCaseSet(endoGrade1Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade1_all",
                "Subtype:  Endometriod:  Grade 1 - All",false, outputDir);
        outputCaseSet(endoGrade1Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade1_core",
                "Subtype:  Endometriod:  Grade 1 - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade2_all",
                "Subtype:  Endometriod:  Grade 2 - All", false, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade2_core",
                "Subtype:  Endometriod:  Grade 2 - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade3_all",
                "Subtype:  Endometriod:  Grade 3 - All", false, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_endo_grade3_core",
                "Subtype:  Endometriod:  Grade 3 - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_serous_all",
                "Subtype:  Endometriod:  Serous - All", false, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_serous_core",
                "Subtype:  Serous - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);

        outputCaseSet(mixedSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_mixed_all",
                "Subtype:  Mixed Serous and Endometriod - All", false, outputDir);
        outputCaseSet(mixedSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, "ucec_tcga_mixed_core",
                "Subtype:  Mixed Serous and Endometriod - Core (Sequenced + GISTIC + RNA-Seq)", true, outputDir);
    }

    private void outputCaseSet(HashSet<String> caseSet, HashSet<String> sequencedCaseSet,
        HashSet<String> gisticCaseSet, RnaSeqReader rnaSeqReader, String stableId, String name, String description, 
        boolean onlyIncludeCoreCases, String outputDir) throws IOException {
        StringBuffer caseIds = new StringBuffer();
        int sampleCount = 0;
        for (String caseId:  caseSet) {
            if (onlyIncludeCoreCases) {
                if (sequencedCaseSet.contains(caseId) && gisticCaseSet.contains(caseId) 
                        && rnaSeqReader.hasRnaReqData(caseId)) {
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
        outWriter.write("case_list_category:  other\n");
        outWriter.write("case_list_name: " + name + "\n");
        outWriter.write("case_list_description: " + description + "\n");
        outWriter.write("case_list_ids:  " + caseIds + "\n");
        outWriter.flush();
        outWriter.close();
    }

    private void outputCaseSet(HashSet<String> caseSet, HashSet<String> sequencedCaseSet,
        HashSet<String> gisticCaseSet, RnaSeqReader rnaSeqReader, String stableId, String name, 
        boolean onlyIncludeSequencedCases,
        String outputDir) throws IOException {
        outputCaseSet(caseSet, sequencedCaseSet, gisticCaseSet, rnaSeqReader, stableId, name, name, 
                onlyIncludeSequencedCases, outputDir);
    }    
}