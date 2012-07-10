package org.mskcc.endometrial.clinical;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.endometrial.cluster.ClusterReader;
import org.mskcc.endometrial.cna.CnaSummarizer;
import org.mskcc.endometrial.cna.CopyNumberMap;
import org.mskcc.endometrial.genomic.GenomicMap;
import org.mskcc.endometrial.mutation.CoverageReader;
import org.mskcc.endometrial.mutation.MutationMap;
import org.mskcc.endometrial.mutation.MutationSummarizer;
import org.mskcc.endometrial.rnaseq.RnaSeqReader;

import java.io.*;
import java.util.*;

/**
 * Prepares the Endometrial Clinical File.
 */
public class PrepareClinicalFile {
    private static final String NA_OUTPUT = "NA";
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";
    private StringBuffer newTable = new StringBuffer();
    private StringBuffer portalTable = new StringBuffer();
    private HashSet<String> sequencedCaseSet;
    private HashSet<String> gisticCaseSet;
    private File mafFile;
    private MsiReader msiReader;
    private CoverageReader coverageReader;
    private RnaSeqReader rnaSeqReader;
    private CaseListUtil caseListUtil = new CaseListUtil();
    private HashSet<String> targetGeneSet;
    private MutationMap mutationMap;
    private CopyNumberMap copyNumberMap;
    private GenomicMap genomicMap;
    private ArrayList<ClusterReader> clusterReaders = new ArrayList<ClusterReader>();

    /**
     * Constructor.

     * @throws IOException IO Error.
     */
    public PrepareClinicalFile(File inputDir) throws IOException, DaoException {
        initTargetGeneSet();
        File clinicalFile = new File(inputDir + "/clinical/clinical.txt");
        File msiFile = new File(inputDir + "/clinical/msi.txt");
        File somaticMafFile = new File(inputDir + "/mutation/UCEC_somatic.maf");
        File cnaFile = new File(inputDir +  "/cna/all_thresholded.by_genes.txt");
        File coverageFile = new File(inputDir + "/mutation/coverage.txt");
        File rnaSeqFile = new File(inputDir + "/rna-seq/rna_seq_rpkm.txt");
        this.mafFile = somaticMafFile;
        System.out.println("Reading in Mutation data...");
        this.mutationMap = new MutationMap(somaticMafFile, targetGeneSet);
        this.copyNumberMap = new CopyNumberMap(cnaFile, targetGeneSet);
        System.out.println("Reading in CNA data...");
        this.genomicMap = new GenomicMap(mutationMap, copyNumberMap);
        System.out.println("Reading in Clinical data...");
        initReaders(inputDir, msiFile, coverageFile, rnaSeqFile);

        CnaSummarizer cnaSummarizer = new CnaSummarizer(cnaFile);
        gisticCaseSet = cnaSummarizer.getGisticCaseSet();
        MutationSummarizer mutationSummarizer = new MutationSummarizer(somaticMafFile);
        sequencedCaseSet = mutationSummarizer.getSequencedCaseSet();
        FileReader reader = new FileReader(clinicalFile);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();  //  The header line.
        this.appendColumnHeaders(line);
        validateHeader(line);
        Map<String,Integer> headers = getHeaderMap(line.split("\t"));

        line = bufferedReader.readLine();
        while (line != null) {
            StringBuffer currentLine = new StringBuffer();
            String parts[] = line.split("\t");
            String caseId = parts[headers.get("tcga_id")];
            String vitalStatus = parts[headers.get("vital_status")];
            String dfsStatus = parts[headers.get("recurred_progressed")];
            String osDays = parts[headers.get("os_days")];
            String dfsDays = parts[headers.get("pdf_days")];

            String histSubTypeAndGrade = parts[headers.get("histology_grade")];
            caseListUtil.categorizeByHistologicalSubType(histSubTypeAndGrade, caseId);
            currentLine.append(line.trim());
            appendMsiStatus(caseId, currentLine);
            appendSequencedColumn(caseId, currentLine);
            appendGenomicDataAvailable(cnaSummarizer, caseId, currentLine);
            appendMutationCounts(mutationSummarizer, caseId, currentLine);
            appendMutationSpectra(mutationSummarizer, caseId, currentLine);
            currentLine.append(TAB + coverageReader.getCoverage(caseId));
            appendClusters(caseId, currentLine);
            appendTargetGeneSet(caseId, currentLine);

            newTable.append(currentLine + NEW_LINE);
            appendPortalSurvival(caseId, vitalStatus, osDays, dfsStatus, dfsDays, currentLine);
            portalTable.append(currentLine + NEW_LINE);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }
    
    private Map<String,Integer> getHeaderMap(String[] headers) {
        Map<String,Integer> map = new HashMap<String,Integer>(headers.length);
        for (int i=0; i<headers.length; i++) {
            map.put(headers[i], i);
        }
        return map;
    }

    private void appendPortalSurvival(String caseId, String vitalStatus, String osDays, String dfsStatus, 
              String dfsDays, StringBuffer currentLine) {
        currentLine.append(TAB + caseId);
        if (vitalStatus.equalsIgnoreCase("Dead")||vitalStatus.equalsIgnoreCase("Deceased")) {
            currentLine.append(TAB + "Deceased");
        } else if (vitalStatus.equalsIgnoreCase("Alive")||vitalStatus.equalsIgnoreCase("Living")) {
            currentLine.append(TAB + "Living");
        } else if (vitalStatus.equalsIgnoreCase("NotAvailable") || vitalStatus.equalsIgnoreCase("Missing")
                || vitalStatus.equalsIgnoreCase("NotApplicable") || vitalStatus.equalsIgnoreCase("Unknown")){
            currentLine.append(TAB + "");
        } else {
            throw new IllegalArgumentException("Unknown Vital Status:  " + vitalStatus);
        }

        try {
            double osMonths = SurvivalCalculator.convertDaysToMonths(osDays);
            currentLine.append(TAB + osMonths);
        } catch (NumberFormatException e) {
            currentLine.append(TAB + "NA");
        }

        if (dfsStatus.equalsIgnoreCase("YES")) {
            currentLine.append(TAB + "Recurred");
        } else if (dfsStatus.equalsIgnoreCase("NO")) {
            currentLine.append(TAB + "DiseaseFree");
        } else if (dfsStatus.equals("NotAvailable") 
                || dfsStatus.equalsIgnoreCase("Missing")
                || dfsStatus.equals("NotApplicable")
                || dfsStatus.equals("")
                || dfsStatus.equalsIgnoreCase("Unknown")) {
            currentLine.append(TAB + "");
        } else {
            throw new IllegalArgumentException("Unknown DFS Status:  " + dfsStatus);
        }

        try {
            double dfsMonths = SurvivalCalculator.convertDaysToMonths(dfsDays);
            currentLine.append(TAB + dfsMonths);
        } catch (NumberFormatException e) {
            currentLine.append(TAB + "NA");
        }
    }

    private void appendClusters(String caseId, StringBuffer currentLine) {
        for (ClusterReader clusterReader:  clusterReaders) {
            ArrayList<String> valueList = clusterReader.getValueList(caseId);
            if (valueList != null) {
                for (String value:  valueList) {
                    currentLine.append(TAB + value);
                }
            } else {
                ArrayList<String> headerList = clusterReader.getHeaderList();
                for (String header:  headerList) {
                    currentLine.append(TAB + NA_OUTPUT);
                }
            }
        }
    }

    private void initTargetGeneSet() {
        targetGeneSet = new HashSet<String>();
        //        targetGeneSet.add("PTEN");
        //        targetGeneSet.add("PIK3CA");
        //        targetGeneSet.add("PIK3R1");
        //        targetGeneSet.add("PIK3R2");
        //        targetGeneSet.add("AKT1");
        //        targetGeneSet.add("AKT2");
        //        targetGeneSet.add("AKT3");
        //        targetGeneSet.add("KRAS");
    }
    
    private String getValue (String targetHeader, String[] colHeaders, String[] parts) {
        for (int i=0; i<colHeaders.length; i++) {
            String currentHeader = colHeaders[i];
            if (currentHeader.equalsIgnoreCase(targetHeader)) {
                return parts[i]; 
            }
        }
        throw new NullPointerException("Could not find column with name:  " + targetHeader);
    }

    private void initReaders(File inputDir, File msiFile, File coverageFile,
                             File rnaSeqFile) throws IOException {
        System.out.println("Reading in MSI Data...");
        msiReader = new MsiReader(msiFile);
        System.out.println("Reading in Sequence Coverage Data...");
        coverageReader = new CoverageReader(coverageFile);

        System.out.println("Reading in RNA-Seq Data...");
        rnaSeqReader = new RnaSeqReader(rnaSeqFile);

        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/cna_clusters.txt")));
        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/mrna_expression_clusters.txt")));
        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/dna_methylation_clusters.txt")));
        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/mlh1_hypermethylated.txt")));
        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/mutation_rate_clusters.txt")));
        clusterReaders.add(new ClusterReader(new File(inputDir + "/clusters/micro_rna_clusters.txt")));
    }
    
    private void appendTargetGeneSet(String caseId, StringBuffer newTable) throws IOException, DaoException {
        Iterator<String> geneIterator = targetGeneSet.iterator();
        while(geneIterator.hasNext()) {
            String geneSymbol = geneIterator.next();
            ArrayList<String> dataFields = genomicMap.getDataFields(geneSymbol, caseId);
            appendColumns(dataFields, newTable);
        }
    }
    
    private void appendMutationSpectra(MutationSummarizer mutationSummarizer, String caseId,
                                       StringBuffer currentLine) {
        currentLine.append(TAB + mutationSummarizer.getTGMutationCount(caseId));
        currentLine.append(TAB + mutationSummarizer.getTCMutationCount(caseId));
        currentLine.append(TAB + mutationSummarizer.getTAMutationCount(caseId));
        currentLine.append(TAB + mutationSummarizer.getCTMutationCount(caseId));
        currentLine.append(TAB + mutationSummarizer.getCGMutationCount(caseId));
        currentLine.append(TAB + mutationSummarizer.getCAMutationCount(caseId));
    }

    private void appendColumnHeaders(String newHeaderLine) {
        newTable.append(newHeaderLine.trim() + TAB
                + "msi_status_7_marker_call" + TAB
                + "msi_status_5_marker_call" + TAB
                + "data_maf" + TAB
                + "data_gistic" + TAB
                + "data_rna_seq" + TAB
                + "data_core_sample" + TAB
                + "silent_mutation_count" + TAB
                + "non_silent_mutation_count" + TAB
                + "total_snv_count" + TAB
                + "indel_mutation_count" + TAB
                + "tg_count" + TAB
                + "tc_count" + TAB
                + "ta_count" + TAB
                + "ct_count" + TAB
                + "cg_count" + TAB
                + "ca_count" + TAB
                + "covered_bases" + TAB);
        for (ClusterReader reader:  clusterReaders) {
            ArrayList<String> headerList = reader.getHeaderList();
            for (String header:  headerList) {
                newTable.append(header + TAB);
            }
        }
        appendTargetGeneSetColumns();
        
        portalTable.append(newTable
            + "CASE_ID" + TAB
            + "OS_STATUS" + TAB
            + "OS_MONTHS" + TAB
            + "DFS_STATUS" + TAB
            + "DFS_MONTHS" + NEW_LINE);
        
        if (newTable.charAt(newTable.length()-1)=='\t') {
            newTable.deleteCharAt(newTable.length()-1);
        }
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

    private void appendMutationCounts(MutationSummarizer mutationSummarizer, String caseId,
              StringBuffer currentLine) {
        long totalSnvCount = mutationSummarizer.getSilentMutationCount(caseId)
                + mutationSummarizer.getNonSilentMutationCount(caseId);
        currentLine.append (TAB + mutationSummarizer.getSilentMutationCount(caseId));
        currentLine.append (TAB + mutationSummarizer.getNonSilentMutationCount(caseId));
        currentLine.append (TAB + totalSnvCount);
        currentLine.append (TAB + mutationSummarizer.getInDelCount(caseId));
    }

    private void appendGenomicDataAvailable(CnaSummarizer cnaSummarizer, String caseId, StringBuffer currentLine) {
        if (cnaSummarizer.hasCnaData(caseId)) {
            currentLine.append (TAB + "Y");
        } else {
            currentLine.append (TAB + "N");
        }
        if (rnaSeqReader.hasRnaReqData(caseId)) {
            currentLine.append (TAB + "Y");
        } else {
            currentLine.append (TAB + "N");
        }
        if (sequencedCaseSet.contains(caseId) && cnaSummarizer.hasCnaData(caseId)
                && rnaSeqReader.hasRnaReqData(caseId)) {
            currentLine.append (TAB + "Y");
        } else {
            currentLine.append (TAB + "N");
        }
    }

    private void appendSequencedColumn(String caseId, StringBuffer currentLine) {
        if (sequencedCaseSet.contains(caseId)) {
            currentLine.append (TAB + "Y");
        } else {
            currentLine.append (TAB + "N");
        }
    }

    private void appendMsiStatus(String caseId, StringBuffer currentLine) {
        String msi5Status = msiReader.getMsi5Status(caseId);
        if (msi5Status != null) {
            currentLine.append(TAB + msi5Status);
        } else {
            currentLine.append(TAB + NA_OUTPUT);
        }
        String msi7Status = msiReader.getMsi7Status(caseId);
        if (msi7Status != null) {
            currentLine.append(TAB + msi5Status);
        } else {
            currentLine.append(TAB + NA_OUTPUT);
        }
    }

    public void writeCaseLists(String outputDir) throws IOException {
        caseListUtil.writeCaseLists(sequencedCaseSet, gisticCaseSet, rnaSeqReader, outputDir);
    }

    public HashSet<String> getSequencedCaseSet() {
        return sequencedCaseSet;
    }

//    /**
//     * The portal expects specific data column names.  This functions transforms into header names that the
//     * portal likes.
//     */
//    private String transformHeader(String headerLine) {
//        headerLine = headerLine.replaceAll("bcr_patient_barcode", "CASE_ID");
//        headerLine = headerLine.replaceAll("NewVitalStatus", "OS_STATUS");
//        return headerLine;
//    }

    /**
     * Validate the Headers.  If the headers have changed, all bets are off and abort.
     */
    private void validateHeader(String header) {
        String parts[] = header.split("\t");
        if (!parts[0].equals("tcga_id")) {
            throw new IllegalArgumentException ("Header at 0 was expecting:  TCGAID");
        }
//        if (!parts[8].equals("vital_status")) {
//            throw new IllegalArgumentException ("Header at 10 was expecting:  VitalStatus");
//        }
    }

    public String getNewClinicalTable() {
        return newTable.toString();
    }

    public String getPortalClinicalTable() {
        return portalTable.toString();
    }

    private void appendColumns(ArrayList<String> list, StringBuffer table) {
        for (String value:  list) {
            table.append(TAB + value);
        }
    }

    public static void main(String[] args) throws Exception {
        // check args
        if (args.length < 1) {
            System.out.println("command line usage:  prepareClinical.pl <input_id>");
            System.exit(1);
        }

        PrepareClinicalFile prepareClinicalFile = new PrepareClinicalFile(new File(args[0]));

        prepareClinicalFile.writeCaseLists(args[0]);

        File newClinicalFile = new File(args[0] + "/clinical/clinical_unified.txt");
        FileWriter writer = new FileWriter(newClinicalFile);
        writer.write(prepareClinicalFile.getNewClinicalTable());

        File portalClinicalFile = new File(args[0] + "/clinical/UCEC.clinical.txt");
        FileWriter portalWriter = new FileWriter(portalClinicalFile);
        portalWriter.write(prepareClinicalFile.getPortalClinicalTable());
        portalWriter.close();

        HashSet <String> sequencedCaseSet = prepareClinicalFile.getSequencedCaseSet();
        System.out.println ("Number of cases sequenced:  " + sequencedCaseSet.size());
        System.out.println ("New Clinical File Written to:  " + newClinicalFile.getAbsolutePath());
        System.out.println ("New Portal Clinical File Written to:  " + portalClinicalFile.getAbsolutePath());
        writer.flush();
        writer.close();
    }
}