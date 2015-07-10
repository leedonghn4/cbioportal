/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ImportDataUtil;

import java.sql.*;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.util.ImportDataUtil;

/**
 *
 * @author dongli
 */
public class DaoCancerStudyGroup {
    private DaoCancerStudyGroup() {}
    
    /**
     * Adds a cancer study to the Database.
     * @param cancerStudy
     * @param overwrite if true, overwrite if exist.
     * @throws DaoException 
     */
    public static int addCancerStudyGroup(CancerStudyGroup cancerStudyGroup) throws DaoException {

        // CANCER_STUDY_IDENTIFIER cannot be null
        String groupIdentifier = cancerStudyGroup.getCancerStudyGroupIdentifier();
        if (groupIdentifier == null) {
            throw new DaoException("Cancer study grou Identifier cannot be null.");
        }
        
        CancerStudyGroup existing = getCancerStudyGroupByIndentifier(groupIdentifier);
        if (existing!=null) {
            if (false) {
                System.out.println("Overwrite cancer study " + groupIdentifier);
                deleteCancerStudyGroup(existing.getInternalId());
            } else {
                throw new DaoException("Cancer study group " + groupIdentifier + "is already imported.");
            }
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        int autoid;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudyGroup.class);
            pstmt = con.prepareStatement("select LAST_INSERT_ID() as last_id from cancer_study_group");
            rs = pstmt.executeQuery();
            
            int lastid = 1;
            
            if(rs.next())
            {
                lastid = rs.getInt("last_id");
            }
                     
            pstmt = con.prepareStatement("INSERT INTO cancer_study_group " +
                    "(`CANCER_STUDY_GROUP_ID`, `CANCER_STUDY_GROUP_IDENTIFIER`, `GROUPNAME` "
                    + ") VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, lastid);
            pstmt.setString(2, cancerStudyGroup.getCancerStudyGroupIdentifier());
            pstmt.setString(3, cancerStudyGroup.getCancerStudyGroupName());

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            rs.next();
            autoid =rs.getInt(1);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudyGroup.class, con, pstmt, rs);
        }
        
        return autoid;
    }

    /**
     * Return the cancerStudy identified by the internal cancer study ID, if it exists.
     *
     * @param cancerStudyGroupID     Internal (int) Cancer Study ID.
     * @return Cancer Study Object, or null if there's no such study.
     */
    public static CancerStudyGroup getCancerStudyGroupByInternalId(int cancerStudyGroupID) throws DaoException {
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        CancerStudyGroup cancerstudygroup;
        
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudyGroup.class);         
            pstmt = con.prepareStatement("SELECT * FROM cancer_study_group WHERE CANCER_STUDY_GROUP_ID = ?");
            pstmt.setInt(1, cancerStudyGroupID);

            rs = pstmt.executeQuery();

            cancerstudygroup = extractCancerStudyGroup(rs);
            
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudyGroup.class, con, pstmt, rs);
        }
                
        return cancerstudygroup;
    }

    /**
     * Returns the cancerStudy identified by the stable identifier, if it exists.
     *
     * @param cancerStudyStableId Cancer Study Stable ID.
     * @return the CancerStudy, or null if there's no such study.
     */
    public static CancerStudyGroup getCancerStudyGroupByIndentifier(String cancerStudyGroupIdentifier) throws DaoException {
        
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        CancerStudyGroup cancerstudygroup;
        
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudyGroup.class);   

            pstmt = con.prepareStatement("SELECT * FROM cancer_study_group WHERE CANCER_STUDY_GROUP_IDENTIFIER = ?", Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, cancerStudyGroupIdentifier);

            rs = pstmt.executeQuery();

            cancerstudygroup = extractCancerStudyGroup(rs);      
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudyGroup.class, con, pstmt, rs);
        }
                
        return cancerstudygroup;
    }

    /**
     * Deletes all Cancer Studies.
     * @throws DaoException Database Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE cancer_study_group");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    public static void deleteCancerStudy(String cancerStudyStableId) throws DaoException
    {
        CancerStudyGroup studygroup = getCancerStudyGroupByIndentifier(cancerStudyStableId);
        if (studygroup != null){
            deleteCancerStudyGroup(studygroup.getInternalId());
        }
    }

    public static String getFreshGroupName(int internalCancerStudyId) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            pstmt = con.prepareStatement("SELECT * FROM cancer_study_group where cancer_study_group_id = ?");
            pstmt.setInt(1, internalCancerStudyId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                CancerStudyGroup cancerStudygroup = extractCancerStudyGroup(rs);
                return cancerStudygroup.getCancerStudyGroupName();
            }
            else {
                return "";
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }
    }

    /**
     * Deletes the Specified Cancer Study.
     *
     * @param internalCancerStudyId Internal Cancer Study ID.
     * @throws DaoException Database Error.
     */
    static void deleteCancerStudyGroup(int internalCancerStudyId) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoCancerStudy.class);
            
            // this is a hacky way to delete all associated data with on cancer study.
            // ideally database dependency should be modeled with option of delete on cascade.
            // remember to update this code if new tables are added or existing tables are changed.
            String[] sqls = {
                "delete from patient where CANCER_STUDY_GROUP_ID=?;",
                "delete from cancer_study where CANCER_STUDY_GROUP_ID=?;",
                "delete from cancer_study_group where CANCER_STUDY_GROUP_ID=?;"
                };
            for (String sql : sqls) {    
                pstmt = con.prepareStatement(sql);
                if (sql.contains("?")) {
                    pstmt.setInt(1, internalCancerStudyId);
                }
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoCancerStudy.class, con, pstmt, rs);
        }

    }

    /**
     * Extracts Cancer Study JDBC Results.
     */
    private static CancerStudyGroup extractCancerStudyGroup(ResultSet rs) throws SQLException {
        
        if(rs.next())
        {
            CancerStudyGroup cancerStudygroup = new CancerStudyGroup(rs.getString("CANCER_STUDY_GROUP_IDENTIFIER"),rs.getString("GROUPNAME"));

            cancerStudygroup.setInternalId(rs.getInt("CANCER_STUDY_GROUP_ID"));
            return cancerStudygroup;
        }
        
        return null;
    }
}
