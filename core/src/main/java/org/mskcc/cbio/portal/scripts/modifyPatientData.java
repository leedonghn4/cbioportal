/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.scripts;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;

import java.io.File;
/**
 *
 * @author dongli
 */
public class modifyPatientData {
    
        public static void main(String[] args) throws Exception {
//        if (args.length == 0) {
//            System.out.println("command line usage: importCancerStudy.pl <cancer_study.txt>");
//            return;
//        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

//        File file = new File(args[0]);
        File file = new File("brca-tcga-public.txt");
        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
        System.out.println ("Loaded the following cancer study:  ");
        System.out.println ("ID:  " + cancerStudy.getInternalId());
        System.out.println ("Name:  " + cancerStudy.getName());
        System.out.println ("Description:  " + cancerStudy.getDescription());
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
    
}
