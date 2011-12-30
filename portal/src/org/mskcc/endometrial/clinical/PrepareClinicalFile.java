package org.mskcc.endometrial.clinical;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;

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

    /**
     * Constructor.
     *
     * @param clinicalFile Clinical File.
     * @param msiFile   MSI File.
     * @throws IOException IO Error.
     */
    public PrepareClinicalFile(File clinicalFile, File msiFile) throws IOException {
        readMsiFile(msiFile);
        FileReader reader = new FileReader(clinicalFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        validateHeader(line);
        String newHeaderLine = transformHeader(line);
        newTable.append(newHeaderLine.trim() + TAB + "DFS_MONTHS" + TAB + "OS_MONTHS" + TAB + "MSI_STATUS" + NEW_LINE);
        line = bufferedReader.readLine();
        while (line != null) {
            String parts[] = line.split("\t");
            String caseId = parts[0];
            String daysToNewTumorEventAfterInitialTreatment = parts[8];
            String vitalStatus = parts[10];
            String daysToFu = parts[12];
            String daysToAlive = parts[13];
            String daysToDead = parts[14];

            // Compute DFS_MONTHS
            computeDfsMonths(caseId, daysToNewTumorEventAfterInitialTreatment);

            // Compute OS_MONTHS
            computeOsMonths(caseId, vitalStatus, daysToFu, daysToAlive, daysToDead);

            newTable.append(line.trim().trim());
            newTable.append(TAB + dfsMonthsMap.get(caseId));
            newTable.append(TAB + osMonthsMap.get(caseId));

            String msiStatus = msiMap.get(caseId);
            if (msiStatus != null) {
                newTable.append(TAB + msiStatus);
            } else {
                newTable.append(TAB + NA_OUTPUT);
            }
            newTable.append(NEW_LINE);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

    /**
     * The portal expects specific data column names.  This functions transforms into header names that the
     * portal likes.
     */
    private String transformHeader(String headerLine) {
        headerLine = headerLine.replaceAll("bcr_patient_barcode", "CASE_ID");
        headerLine = headerLine.replaceAll("NewVitalStatus", "OS_STATUS");
        headerLine = headerLine.replaceAll("Recurred/Progressed", "DFS_STATUS");
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

    private void computeOsMonths(String caseId, String vitalStatus, String daysToFu, String daysToAlive,
            String daysToDead) {
        String osMonthsStr = NA_OUTPUT;
        if (vitalStatus.equals(DECEASED)) {
            double osMonths = convertDaysToMonths(daysToDead);
            osMonthsStr = formatter.format(osMonths);
        } else if (vitalStatus.equals(LIVING)) {
            if (daysToAlive.trim().length() > 0 && !daysToAlive.equals(NA_INPUT)) {
                double osMonths = convertDaysToMonths(daysToAlive);
                osMonthsStr = formatter.format(osMonths);
            } else if (daysToFu.trim().length() > 0 && !daysToFu.equals(NA_INPUT)) {
                double osMonths = convertDaysToMonths(daysToFu);
                osMonthsStr = formatter.format(osMonths);
            }
        } else {
            throw new IllegalArgumentException("Aborting.  Cannot process VITAL STATUS:  " + vitalStatus);
        }
        osMonthsMap.put(caseId, osMonthsStr);
    }

    private void computeDfsMonths(String caseId, String daysToNewTumorEventAfterInitialTreatment) {
        String dfsMonthsStr = NA_OUTPUT;
        if (daysToNewTumorEventAfterInitialTreatment.trim().length() > 0) {
            double dfsMonths = convertDaysToMonths(daysToNewTumorEventAfterInitialTreatment);
            dfsMonthsStr = formatter.format(dfsMonths);
        }
        dfsMonthsMap.put(caseId, dfsMonthsStr);
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
            System.out.println("command line usage:  prepareClinical.pl <clinical_file> <msi_file> <output_file>");
            System.exit(1);
        }
        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile(new File(args[0]),
                new File(args[1]));

        FileWriter writer = new FileWriter(new File(args[2]));
        writer.write(prepareClinicalFile.getNewClinicalTable());
        System.out.println ("New Clinical File Written to:  " + args[2]);
        writer.flush();
        writer.close();
    }
}