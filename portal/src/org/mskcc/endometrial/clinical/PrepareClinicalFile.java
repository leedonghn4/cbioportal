package org.mskcc.endometrial.clinical;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;

/**
 * Prepares the Endometrial Clinical File.
 * <p/>
 * Does the following:
 * <p/>
 * 1.  Calculates OS_MONTHS
 * 2.  Calculates DFS_MONTHS
 * 3.  Merges in MSI Values
 * <p/>
 * We assume the clinical file has the following headers:
 * 0:  bcr_patient_barcode
 * 1:  age_at_initial_pathologic_diagnosis
 * 2:  days_to_birth
 * 3:  2009FIGOstageCorrected
 * 4:  histological_typeCorrected
 * 5:  tumor_grade
 * 6:  year_of_initial_pathologic_diagnosis
 * 7:  primaryTherapyOutcomeSuccess
 * 8:  daysToNewTumorEventAfterInitialTreatment
 * 9:  Recurred/Progressed
 * 10:  NewVitalStatus
 * 11:  NewPersNeoplasmStatus
 * 12:  NewDaystoFU
 * 13:  NewDaystoAlive
 * 14:  NewDaystoDead
 * <p/>
 * We also assume the MSI file has the following headers:
 * 0:  tcga_id
 * 1:  TCGA ID
 * 2:  BAT40
 * 3:  BAT26
 * 4:  BAT25
 * 5:  D17S250
 * 6:  TGFBII
 * 7:  D5S346
 * 8:  D2S123
 * 9:  Penta D
 * 10:  Penta E
 * 11:  MSI CLASS
 */
public class PrepareClinicalFile {
    private static final double ONE_DAY = 0.0328549112;
    private static final String LIVING = "LIVING";
    private static final String DECEASED = "DECEASED";
    private static final String NA_INPUT = "[Not Available]";
    private static final String NA_OUTPUT = "NA";
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";
    private DecimalFormat formatter = new DecimalFormat("###.##");
    private HashMap<String, String> osMonthsMap = new HashMap<String, String>();
    private HashMap<String, String> dfsMonthsMap = new HashMap<String, String>();
    private StringBuffer newTable = new StringBuffer();
    private HashMap<String, String> msiMap = new HashMap<String, String>();
    private HashSet<String> sequencedCaseSet = new HashSet<String>();
    private HashSet<String> endoGrade1Set = new HashSet<String>();
    private HashSet<String> endoGrade2Set = new HashSet<String>();
    private HashSet<String> endoGrade3Set = new HashSet<String>();
    private HashSet<String> serousSet = new HashSet<String>();
    private HashSet<String> mixedSet = new HashSet<String>();

    /**
     * Constructor.
     *
     * @param clinicalFile Clinical File.
     * @param msiFile   MSI File.
     * @param mafFile   MAF Mutation File.
     * @throws IOException IO Error.
     */
    public PrepareClinicalFile(File clinicalFile, File msiFile, File mafFile) throws IOException {
        readMsiFile(msiFile);
        skimMafFile(mafFile);
        FileReader reader = new FileReader(clinicalFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        validateHeader(line);
        String newHeaderLine = transformHeader(line);
        newTable.append(newHeaderLine.trim() + TAB + "DFS_STATUS" + TAB + "DFS_MONTHS" + TAB + "OS_MONTHS" + TAB
                + "MSI_STATUS" + TAB + "SEQUENCED" + NEW_LINE);
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String caseId = parts[0];
            String histSubType = parts[4];
            String daysToNewTumorEventAfterInitialTreatment = parts[8];
            String vitalStatus = parts[10];
            String recurredStatus = parts[9];
            String daysToFu = parts[12];
            String daysToAlive = parts[13];
            String daysToDead = parts[14];

            // Compute DFS_MONTHS
            computeDfsMonths(caseId, recurredStatus, daysToNewTumorEventAfterInitialTreatment, daysToFu);

            // Compute OS_MONTHS
            computeOsMonths(caseId, vitalStatus, daysToFu, daysToAlive, daysToDead);

            newTable.append(line.trim().trim());

            if (recurredStatus.equalsIgnoreCase("YES")) {
                newTable.append(TAB + "Recurred");
            } else if (recurredStatus.equals("NO")) {
                newTable.append(TAB + "DiseaseFree");
            } else {
                newTable.append(TAB + "");
            }
            newTable.append(TAB + dfsMonthsMap.get(caseId));
            newTable.append(TAB + osMonthsMap.get(caseId));

            String msiStatus = msiMap.get(caseId);
            if (msiStatus != null) {
                newTable.append(TAB + msiStatus);
            } else {
                newTable.append(TAB + NA_OUTPUT);
            }

            if (sequencedCaseSet.contains(caseId)) {
                newTable.append (TAB + "Y");
            } else {
                newTable.append (TAB + "N");
            }

            categorizeByHistologicalSubType(histSubType, caseId);
            newTable.append(NEW_LINE);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    public void writeCaseLists(String outputDir) throws IOException {
        outputCaseSet(endoGrade1Set, sequencedCaseSet, "ucec_endo_grade1_all",
                "Subtype:  Endometrial:  Grade 1 - All",false, outputDir);
        outputCaseSet(endoGrade1Set, sequencedCaseSet, "ucec_endo_grade1_sequenced",
                "Subtype:  Endometrial:  Grade 1 - Sequenced", true, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, "ucec_endo_grade2_all",
                "Subtype:  Endometrial:  Grade 2 - All", false, outputDir);
        outputCaseSet(endoGrade2Set, sequencedCaseSet, "ucec_endo_grade2_sequenced",
                "Subtype:  Endometrial:  Grade 2 - Sequenced", true, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, "ucec_endo_grade3_all",
                "Subtype:  Endometrial:  Grade 3 - All", false, outputDir);
        outputCaseSet(endoGrade3Set, sequencedCaseSet, "ucec_endo_grade3_sequenced",
                "Subtype:  Endometrial:  Grade 3 - Sequenced", true, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, "ucec_serous_all",
                "Subtype:  Endometrial:  Serous - All", false, outputDir);
        outputCaseSet(serousSet, sequencedCaseSet, "ucec_serous_sequenced",
                "Subtype:  Endometrial:  Serous - Sequenced", true, outputDir);

        outputCaseSet(mixedSet, sequencedCaseSet, "ucec_mixed_all",
                "Subtype:  Mixed Serous and Endometrial - All", false, outputDir);
        outputCaseSet(mixedSet, sequencedCaseSet, "ucec_mixed_sequenced",
                "Subtype:  Mixed Serous and Endometrial - Sequenced", true, outputDir);
        
        HashSet<String> allEndoSet = new HashSet<String>();
        allEndoSet.addAll(endoGrade1Set);
        allEndoSet.addAll(endoGrade2Set);
        allEndoSet.addAll(endoGrade3Set);
        outputCaseSet(allEndoSet, sequencedCaseSet, "ucec_endo_all",
                "Subtype:  Endometrial:  Grades 1-3 - All",false, outputDir);
        outputCaseSet(allEndoSet, sequencedCaseSet, "ucec_endo_sequenced",
                "Subtype:  Endometrial:  Grades 1-3 - Sequenced", true, outputDir);
    }

    private void outputCaseSet(HashSet<String> caseSet, HashSet<String> sequencedCaseSet,
            String stableId, String name, boolean onlyIncludeSequencedCases,
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
        String description = name + " [Auto generated on " + new Date() + "].";
        File outputFile = new File(outputDir + "/case_lists/" + stableId + ".txt");
        System.out.println ("Writing case set:  " + outputFile.getAbsolutePath());
        FileWriter outWriter = new FileWriter(outputFile);
        outWriter.write("cancer_study_identifier: ucec_tcga\n");
        outWriter.write("stable_id: " + stableId + "\n");
        outWriter.write("case_list_name: " + name + "\n");
        outWriter.write("case_list_description: " + description + "\n");
        outWriter.write("case_list_ids:  " + caseIds + "\n");
        outWriter.flush();
        outWriter.close();
    }

    private void categorizeByHistologicalSubType(String histSubType, String caseId) {
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

    public HashSet<String> getSequencedCaseSet() {
        return sequencedCaseSet;
    }

    /**
     * The portal expects specific data column names.  This functions transforms into header names that the
     * portal likes.
     */
    private String transformHeader(String headerLine) {
        headerLine = headerLine.replaceAll("bcr_patient_barcode", "CASE_ID");
        headerLine = headerLine.replaceAll("NewVitalStatus", "OS_STATUS");
        return headerLine;
    }

    /**
     * Validate the Headers.  If the headers have changed, all bets are off and abort.
     */
    private void validateHeader(String header) {
        String parts[] = header.split("\t");
        if (!parts[0].equals("bcr_patient_barcode")) {
            throw new IllegalArgumentException ("Header at 0 was expecting:  bcr_patient_barcode");
        }
        if (!parts[10].equals("NewVitalStatus")) {
            throw new IllegalArgumentException ("Header at 10 was expecting:  NewVitalStatus");
        }
        if (!parts[12].equals("NewDaystoFU")) {
            throw new IllegalArgumentException ("Header at 12 was expecting:  NewDaystoFU");
        }
        if (!parts[13].equals("NewDaystoAlive")) {
            throw new IllegalArgumentException ("Header at 13 was expecting:  NewDaystoAlive");
        }
        if (!parts[14].equals("NewDaystoDead")) {
            throw new IllegalArgumentException ("Header at 14 was expecting:  NewDaystoDead");
        }
    }

    public String getMsiStatus(String caseId) {
        return msiMap.get(caseId);
    }

    private void readMsiFile(File msiFile) throws IOException {
        FileReader reader = new FileReader(msiFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String barCode = parts[1];
            String msiClass = parts[11];

            //  bar code ids look like this:  TCGA-A6-2671-01A-01D-1861-23
            String idParts[] = barCode.split("-");
            if (barCode.trim().length()>0) {
                String caseId = "TCGA-" + idParts[0] + "-" + idParts[1];
                msiMap.put(caseId, msiClass);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private void skimMafFile(File mafFile) throws IOException {
        FileReader reader = new FileReader(mafFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String headerLine = bufferedReader.readLine();  //  The header line.
        int caseIdIndex = getCaseIdIndex(headerLine);
        String line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");

            String barCode = parts[caseIdIndex];
            String barCodeParts[] = barCode.split("-");
            String caseId = null;
            try {
                caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];
            } catch( ArrayIndexOutOfBoundsException e) {
                caseId = barCode;
            }
            if (!sequencedCaseSet.contains(caseId)) {
                sequencedCaseSet.add(caseId);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    private int getCaseIdIndex(String headerLine) {
        String parts[] = headerLine.split("\t");
        for (int i=0; i<parts.length; i++) {
            String headerName = parts[i];
            if (headerName.equals("Tumor_Sample_Barcode")) {
                return i;
            }
        }
        return -1;
    }

    private void computeOsMonths(String caseId, String vitalStatus, String daysToFu, String daysToAlive,
            String daysToDead) {
        String osMonthsStr = NA_OUTPUT;
        if (vitalStatus.equals(DECEASED)) {
            double osMonths = convertDaysToMonths(daysToDead);
            osMonthsStr = formatter.format(osMonths);
        } else if (vitalStatus.equals(LIVING)) {
            boolean daysToAliveHasData = fieldHasData(daysToAlive);
            boolean daysToFUHasData = fieldHasData(daysToFu);
            if (daysToAliveHasData && daysToFUHasData) {
                double osMonths1 = convertDaysToMonths(daysToAlive);
                double osMonths2= convertDaysToMonths(daysToFu);
                double osMonths = Math.max(osMonths1, osMonths2);
                osMonthsStr = formatter.format(osMonths);
            } else if (daysToAliveHasData) {
                double osMonths = convertDaysToMonths(daysToAlive);
                osMonthsStr = formatter.format(osMonths);
            } else if (daysToFUHasData) {
                double osMonths = convertDaysToMonths(daysToFu);
                osMonthsStr = formatter.format(osMonths);
            } else {
                osMonthsStr = NA_OUTPUT;
            }
        } else {
            throw new IllegalArgumentException("Aborting.  Cannot process VITAL STATUS:  " + vitalStatus);
        }
        osMonthsMap.put(caseId, osMonthsStr);
    }

    private void computeDfsMonths(String caseId, String recurredStatus,
            String daysToNewTumorEventAfterInitialTreatment, String daysToFollowUp) {
        String dfsMonthsStr = NA_OUTPUT;

        if (recurredStatus.equalsIgnoreCase("YES")) {
            if (fieldHasData(daysToNewTumorEventAfterInitialTreatment)) {
                double dfsMonths = convertDaysToMonths(daysToNewTumorEventAfterInitialTreatment);
                dfsMonthsStr = formatter.format(dfsMonths);
            } else {
                dfsMonthsStr = NA_OUTPUT;
            }
        } else if (recurredStatus.equalsIgnoreCase("NO")) {
            if (fieldHasData(daysToFollowUp)) {
                double dfsMonths = convertDaysToMonths(daysToFollowUp);
                dfsMonthsStr = formatter.format(dfsMonths);
            } else {
                dfsMonthsStr = NA_OUTPUT;
            }
        } else {
            dfsMonthsStr = NA_OUTPUT;
        }
        dfsMonthsMap.put(caseId, dfsMonthsStr);
    }

    private boolean fieldHasData(String fieldValue) {
        if (fieldValue.trim().length() > 0) {
            if (!fieldValue.equalsIgnoreCase(NA_INPUT)) {
                return true;
            }
        }
        return false;
    }

    public String getDfsMonths(String caseId) {
        return dfsMonthsMap.get(caseId);
    }

    public String getOsMonths(String caseId) {
        return osMonthsMap.get(caseId);
    }

    public String getNewClinicalTable() {
        return newTable.toString();
    }

    private double convertDaysToMonths(String numberOfDays) {
        int numDays = Integer.parseInt(numberOfDays);
        return numDays * ONE_DAY;
    }

    public static void main(String[] args) throws Exception {
        // check args
        if (args.length < 3) {
            System.out.println("command line usage:  prepareClinical.pl <clinical_file> <msi_file> <maf_file> " +
                    "<output_dir>");
            System.exit(1);
        }
        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile(new File(args[0]),
                new File(args[1]), new File(args[2]));

        prepareClinicalFile.writeCaseLists(args[3]);

        File newClinicalFile = new File(args[3] + "/ucec_clinical_unified.txt");
        FileWriter writer = new FileWriter(newClinicalFile);
        writer.write(prepareClinicalFile.getNewClinicalTable());

        HashSet <String> sequencedCaseSet = prepareClinicalFile.getSequencedCaseSet();
        System.out.println ("Number of cases sequenced:  " + sequencedCaseSet.size());
        System.out.println ("New Clinical File Written to:  " + newClinicalFile.getAbsolutePath());
        writer.flush();
        writer.close();
    }
}