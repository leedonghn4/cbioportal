/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.scripts;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.DaoDrug;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import static org.mskcc.cbio.portal.dao.DaoPatient.cachePatient;
import org.mskcc.cbio.portal.dao.JdbcUtil;
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
     
        List<Patient> list = DaoPatient.getAllPatient();
        
        
        for(int i = 0; i<list.size();i++)
        {
            Patient patientValue = list.get(i);
            if(patientValue.getCancerStudy()!= null)
            {
                String cancerstudyid = patientValue.getStableId();
                DaoPatient.updatePatient(patientValue);
            }
        }      
    }
    
}
