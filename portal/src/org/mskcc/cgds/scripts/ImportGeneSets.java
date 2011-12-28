package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Command Line Tool to Import Gene Sets in MSIG_DB Format.
 */
public class ImportGeneSets {
    private ProgressMonitor pMonitor;
    private File geneSetFile;

    public ImportGeneSets(File geneSetFile, ProgressMonitor pMonitor) {
        this.geneSetFile = geneSetFile;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        DaoGeneSet daoGeneSet = new DaoGeneSet();
        FileReader reader = new FileReader(geneSetFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            String parts[] = line.split("\t");
            String name = parts[0];
            pMonitor.setCurrentMessage("Adding gene set:  " + name);
            String description = parts[1];
            ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
            for (int i=2; i< parts.length; i++) {
                long entrezGeneId = Long.parseLong(parts[i]);
                CanonicalGene currentGene = daoGene.getGene(entrezGeneId);
                if (currentGene != null) {
                    geneList.add(currentGene);
                } else {
                    pMonitor.setCurrentMessage("Cannot find gene:  " + entrezGeneId);
                }
            }
            daoGeneSet.addGeneSet(name, description, geneList);
            line = buf.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importGeneSets.pl <gene_set_file.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File geneFile = new File(args[0]);
        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportGeneSets parser = new ImportGeneSets(geneFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}