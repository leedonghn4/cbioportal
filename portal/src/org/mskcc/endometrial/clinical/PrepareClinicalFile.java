package org.mskcc.endometrial.clinical;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.endometrial.cna.CnaClusterReader;
import org.mskcc.endometrial.cna.CnaSummarizer;
import org.mskcc.endometrial.cna.CopyNumberMap;
import org.mskcc.endometrial.genomic.GenomicMap;
import org.mskcc.endometrial.methylation.MethylationReader;
import org.mskcc.endometrial.mutation.CoverageReader;
import org.mskcc.endometrial.mutation.GermlineMutationSummarizer;
import org.mskcc.endometrial.mutation.MutationMap;
import org.mskcc.endometrial.mutation.MutationSummarizer;
import org.mskcc.endometrial.rppa.RppaReader;

import java.io.*;
import java.util.*;

/**
 * Prepares the Endometrial Clinical File.
 */
public class PrepareClinicalFile {
    private static final String NA_OUTPUT = "NA";
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";
    private HashMap<String, String> osMonthsMap = new HashMap<String, String>();
    private HashMap<String, String> dfsMonthsMap = new HashMap<String, String>();
    private StringBuffer newTable = new StringBuffer();
    private HashSet<String> sequencedCaseSet;
    private File mafFile;
    private CnaClusterReader cnaClusterReader;
    private MsiReader msiReader;
    private MethylationReader mlh1Reader;
    private RppaReader rppaReader;
    private CoverageReader coverageReader;
    private CaseListUtil caseListUtil = new CaseListUtil();
    private HashSet<String> targetGeneSet;
    private MutationMap mutationMap;
    private CopyNumberMap copyNumberMap;
    private GenomicMap genomicMap;

    /**
     * Constructor.

     * @throws IOException IO Error.
     */
    public PrepareClinicalFile(File clinicalFile, File msiFile, File somaticMafFile, 
            File germlineMafFile, File cnaFile, File cnaClusterFile, File mlh1MethFile, File coverageFile,
            File rppaFile, boolean performSanityChecks) throws IOException, DaoException {
        initTargetGeneSet();
        this.mafFile = somaticMafFile;
        System.out.println("Reading in mutation data...");
        this.mutationMap = new MutationMap(somaticMafFile, targetGeneSet);
        this.copyNumberMap = new CopyNumberMap(cnaFile, targetGeneSet);
        System.out.println("Reading in CNA data...");
        this.genomicMap = new GenomicMap(mutationMap, copyNumberMap);

        initReaders(msiFile, cnaClusterFile, mlh1MethFile, coverageFile, rppaFile);

        CnaSummarizer cnaSummarizer = new CnaSummarizer(cnaFile);
        MutationSummarizer mutationSummarizer = new MutationSummarizer(somaticMafFile);
        GermlineMutationSummarizer germlineMutationSummarizer = new
                GermlineMutationSummarizer(germlineMafFile, performSanityChecks);
        sequencedCaseSet = mutationSummarizer.getSequencedCaseSet();
        FileReader reader = new FileReader(clinicalFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        validateHeader(line);
        String newHeaderLine = transformHeader(line);
        appendColumnHeaders(germlineMutationSummarizer, newHeaderLine);
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
            caseListUtil.categorizeByHistologicalSubType(histSubType, caseId);

            computeDfsMonths(caseId, recurredStatus, daysToNewTumorEventAfterInitialTreatment, daysToFu);
            computeOsMonths(caseId, vitalStatus, daysToFu, daysToAlive, daysToDead);
            newTable.append(line.trim());

            appendSurvivalColumns(recurredStatus, caseId);
            appendMsiStatus(caseId);
            appendSequencedColumn(caseId);
            appendCnaDataAvailableColumns(cnaSummarizer, caseId);
            appendCnaColumns(cnaSummarizer, caseId);
            appendCnaClusterColumn(caseId);
            appendMutationCounts(mutationSummarizer, caseId);
            appendMlh1MethylationStatus(caseId);
            appendMutationSpectra(mutationSummarizer, caseId);
            newTable.append(TAB + coverageReader.getCoverage(caseId));
            appendGermlineMutationFields(germlineMutationSummarizer, caseId, newTable);
            appendTargetGeneSet(caseId, newTable);
            appendRppaData(caseId, newTable);
            newTable.append(NEW_LINE);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }
    
    private void initTargetGeneSet() {
        targetGeneSet = new HashSet<String>();
        targetGeneSet.add("PTEN");
        targetGeneSet.add("PIK3CA");
        targetGeneSet.add("PIK3R1");
        targetGeneSet.add("PIK3R2");
        targetGeneSet.add("AKT1");
        targetGeneSet.add("AKT2");
        targetGeneSet.add("AKT3");
    }

    private void initReaders(File msiFile, File cnaClusterFile, File mlh1MethFile, File coverageFile,
             File rppaFile) throws IOException {
        System.out.println("Reading in MSI Data...");
        msiReader = new MsiReader(msiFile);
        System.out.println("Reading in MLH1 Hypermethylation Data...");
        mlh1Reader = new MethylationReader(mlh1MethFile);
        System.out.println("Reading in Sequence Coverage Data...");
        coverageReader = new CoverageReader(coverageFile);
        System.out.println("Reading in CNA Clusters...");
        cnaClusterReader = new CnaClusterReader(cnaClusterFile);
        rppaReader = new RppaReader(rppaFile);
    }
    
    private void appendTargetGeneSet(String caseId, StringBuffer newTable) throws IOException, DaoException {
        Iterator<String> geneIterator = targetGeneSet.iterator();
        while(geneIterator.hasNext()) {
            String geneSymbol = geneIterator.next();
            ArrayList<String> dataFields = genomicMap.getDataFields(geneSymbol, caseId);
            appendColumns(dataFields, newTable);
        }
    }
    
    private void appendRppaData(String caseId, StringBuffer newTable) {
        ArrayList<String> dataFields = rppaReader.getDataValues(caseId);
        appendColumns(dataFields, newTable);
    }

    private void appendMutationSpectra(MutationSummarizer mutationSummarizer, String caseId) {
        newTable.append(TAB + mutationSummarizer.getTGMutationCount(caseId));
        newTable.append(TAB + mutationSummarizer.getTCMutationCount(caseId));
        newTable.append(TAB + mutationSummarizer.getTAMutationCount(caseId));
        newTable.append(TAB + mutationSummarizer.getCTMutationCount(caseId));
        newTable.append(TAB + mutationSummarizer.getCGMutationCount(caseId));
        newTable.append(TAB + mutationSummarizer.getCAMutationCount(caseId));
    }

    private void appendMlh1MethylationStatus(String caseId) {
        if (mlh1Reader.getMethylationStatus(caseId) != null) {
            newTable.append(TAB + mlh1Reader.getMethylationStatus(caseId));
        } else {
            newTable.append(TAB + NA_OUTPUT);
        }
    }

    private void appendColumnHeaders(GermlineMutationSummarizer germlineMutationSummarizer, String newHeaderLine) {
        newTable.append(newHeaderLine.trim() + TAB
                + "DFS_STATUS" + TAB
                + "DFS_MONTHS" + TAB
                + "OS_MONTHS" + TAB
                + "MSI_STATUS" + TAB
                + "SEQUENCED" + TAB
                + "GISTIC" + TAB
                + "SEQUENCED_AND_GISTIC" + TAB
                + "CNA_ALTERED_1" + TAB
                + "CNA_ALTERED_2" + TAB
                + "CNA_CLUSTER" + TAB
                + "SILENT_MUTATION_COUNT" + TAB
                + "NON_SILENT_MUTATION_COUNT" + TAB
                + "TOTAL_SNV_COUNT" +  TAB
                + "INDEL_MUTATION_COUNT" + TAB
                + "MLH1_HYPERMETHYLATED" + TAB
                + "TG_COUNT" + TAB
                + "TC_COUNT" + TAB
                + "TA_COUNT" + TAB
                + "CT_COUNT" + TAB
                + "CG_COUNT" + TAB
                + "CA_COUNT" + TAB
                + "COVERED_BASES");
        appendGermlineMutationColumns(germlineMutationSummarizer, newTable);
        appendTargetGeneSetColumns();
        appendRppaColumns();
        newTable.append(NEW_LINE);
    }

    private void appendTargetGeneSetColumns() {
        Iterator<String> geneIterator = targetGeneSet.iterator();
        while(geneIterator.hasNext()) {
            String geneSymbol = geneIterator.next();
            ArrayList<String> headingList = genomicMap.getColumnHeaders(geneSymbol);
            appendColumns(headingList, newTable);
        }
    }
    
    private void appendRppaColumns() {
        ArrayList<String> headingList = rppaReader.getRppaHeaders();
        appendColumns(headingList, newTable);
    }
    
    private void appendMutationCounts(MutationSummarizer mutationSummarizer, String caseId) {
        long totalSnvCount = mutationSummarizer.getSilentMutationCount(caseId)
                + mutationSummarizer.getNonSilentMutationCount(caseId);
        newTable.append (TAB + mutationSummarizer.getSilentMutationCount(caseId));
        newTable.append (TAB + mutationSummarizer.getNonSilentMutationCount(caseId));
        newTable.append (TAB + totalSnvCount);
        newTable.append (TAB + mutationSummarizer.getInDelCount(caseId));
    }

    private void appendCnaDataAvailableColumns(CnaSummarizer cnaSummarizer, String caseId) {
        if (cnaSummarizer.hasCnaData(caseId)) {
            newTable.append (TAB + "Y");
        } else {
            newTable.append (TAB + "N");
        }
        if (sequencedCaseSet.contains(caseId) && cnaSummarizer.hasCnaData(caseId)) {
            newTable.append (TAB + "Y");
        } else {
            newTable.append (TAB + "N");
        }
    }

    private void appendCnaColumns(CnaSummarizer cnaSummarizer, String caseId) {
        newTable.append (TAB + cnaSummarizer.getCna1Count(caseId));
        newTable.append (TAB + cnaSummarizer.getCna2Count(caseId));
    }

    private void appendCnaClusterColumn(String caseId) {
        String cnaCluster = cnaClusterReader.getCnaClusterAssignment(caseId);
        if (cnaCluster == null) {
            newTable.append (TAB + NA_OUTPUT);
        } else {
            newTable.append (TAB + cnaCluster);
        }
    }

    private void appendSequencedColumn(String caseId) {
        if (sequencedCaseSet.contains(caseId)) {
            newTable.append (TAB + "Y");
        } else {
            newTable.append (TAB + "N");
        }
    }

    private void appendMsiStatus(String caseId) {
        String msiStatus = msiReader.getMsiStatus(caseId);
        if (msiStatus != null) {
            newTable.append(TAB + msiStatus);
        } else {
            newTable.append(TAB + NA_OUTPUT);
        }
    }

    private void appendSurvivalColumns(String recurredStatus, String caseId) {
        if (recurredStatus.equalsIgnoreCase("YES")) {
            newTable.append(TAB + "Recurred");
        } else if (recurredStatus.equalsIgnoreCase("NO")) {
            newTable.append(TAB + "DiseaseFree");
        } else {
            newTable.append(TAB + "");
        }
        newTable.append(TAB + dfsMonthsMap.get(caseId));
        newTable.append(TAB + osMonthsMap.get(caseId));
    }

    public void writeCaseLists(String outputDir) throws IOException {
        caseListUtil.writeCaseLists(sequencedCaseSet, cnaClusterReader, outputDir);
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

    private void computeOsMonths(String caseId, String vitalStatus, String daysToFu, String daysToAlive,
            String daysToDead) {
        String osMonthsStr = SurvivalCalculator.calculateOsMonths(vitalStatus, daysToFu,
                daysToAlive, daysToDead);
        osMonthsMap.put(caseId, osMonthsStr);
    }

    private void computeDfsMonths(String caseId, String recurredStatus,
            String daysToNewTumorEventAfterInitialTreatment, String daysToFollowUp) {
        String dfsMonthsStr = SurvivalCalculator.calculateDfsMonths(recurredStatus,
                daysToNewTumorEventAfterInitialTreatment, daysToFollowUp);
        dfsMonthsMap.put(caseId, dfsMonthsStr);
    }

    public String getNewClinicalTable() {
        return newTable.toString();
    }

    private void appendGermlineMutationColumns(GermlineMutationSummarizer germlineMutationSummarizer,
            StringBuffer newTable) {
        ArrayList<String> headingList = germlineMutationSummarizer.getColumnHeadings();
        appendColumns(headingList, newTable);
    }

    private void appendGermlineMutationFields(GermlineMutationSummarizer germlineMutationSummarizer,
            String caseId, StringBuffer newTable) {
        ArrayList<String> valueList = germlineMutationSummarizer.getValues(caseId);
        appendColumns(valueList, newTable);
    }

    private void appendColumns(ArrayList<String> list, StringBuffer table) {
        for (String value:  list) {
            table.append(TAB + value);
        }
    }

    public static void main(String[] args) throws Exception {
        // check args
        if (args.length < 4) {
            System.out.println("command line usage:  prepareClinical.pl <clinical_file> " +
                    "<msi_file> <somatic_maf_file> <germline_maf_file> " +
                    "<cna_file> <cna_clusters_file> <mlh1_meth_file> <output_dir>");
            System.exit(1);
        }

        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile(new File(args[0]),
                new File(args[1]), new File(args[2]), new File(args[3]), new File(args[4]),
                new File(args[5]), new File(args[6]), new File(args[7]), new File(args[8]), true);

        prepareClinicalFile.writeCaseLists(args[9]);

        File newClinicalFile = new File(args[9] + "/ucec_clinical_unified.txt");
        FileWriter writer = new FileWriter(newClinicalFile);
        writer.write(prepareClinicalFile.getNewClinicalTable());

        HashSet <String> sequencedCaseSet = prepareClinicalFile.getSequencedCaseSet();
        System.out.println ("Number of cases sequenced:  " + sequencedCaseSet.size());
        System.out.println ("New Clinical File Written to:  " + newClinicalFile.getAbsolutePath());
        writer.flush();
        writer.close();
    }
}