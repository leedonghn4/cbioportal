/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCancerStudyGroup;
import org.mskcc.cbio.portal.model.CancerStudyGroup;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.CancerStudyReader;
import static org.mskcc.cbio.portal.util.CancerStudyReader.loadCancerStudy;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author dongli
 */
public class importCancerStudyGroup {
    
        public static CancerStudyGroup loadCancerStudygroup(File file) throws IOException, DaoException {
            
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));

            CancerStudyGroup cancerstudygroup = getCancerStudyGroup(properties);
            
            int autogroupid = DaoCancerStudyGroup.addCancerStudyGroup(cancerstudygroup); // overwrite if exist
            
            return cancerstudygroup;
        }
        
    private static CancerStudyGroup getCancerStudyGroup(Properties properties)
    {
        String cancerStudyGroupIdendifier = properties.getProperty("cancer_study_group_identifier");
        
        String cancerStudyGroupName = properties.getProperty("cancer_study_group_name");

        CancerStudyGroup cancerstudygroup = new CancerStudyGroup(cancerStudyGroupIdendifier,cancerStudyGroupName);

        return cancerstudygroup;
    }
    
    public static void main(String[] args) throws Exception {
//        if (args.length == 0) {
//            System.out.println("command line usage: importCancerStudy.pl <cancer_study.txt>");
//            return;
//        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File("brca-tcga-public.txt");
        
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        CancerStudyGroup cancerstudygroup = loadCancerStudygroup(file);
                
        System.out.println ("Loaded the following cancer study:  ");
        System.out.println ("ID:  " + cancerstudygroup.getInternalId());
        System.out.println ("Name:  " + cancerstudygroup.getCancerStudyGroupName());
        System.out.println ("Identifier:  " + cancerstudygroup.getCancerStudyGroupIdentifier());
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
