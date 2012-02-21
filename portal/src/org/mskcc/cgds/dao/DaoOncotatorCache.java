package org.mskcc.cgds.dao;

import java.sql.*;

/**
 * Cache for Oncotator.
 *
 * @author Ethan Cerami
 */
public class DaoOncotatorCache {

    /**
     * Adds an Oncotator Record.
     *
     * @param key   Oncotator Key.
     * @param json  Oncotator JSON.
     * @throws DaoException Database Error.
     */
    public static void addOncotatorRecord(String key, String json) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("INSERT INTO oncotator (`CACHE_KEY`, `JSON`) VALUES (?,?)");
            pstmt.setString(1, key);
            pstmt.setString(2, json);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets the Specified Oncotator Record.
     * @param key Oncotator Key.
     * @return Oncotator Record.
     * @throws DaoException Database Error.
     */
    public static String getOncotatorRecord(String key) throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("SELECT * FROM oncotator WHERE CACHE_KEY = ?");
            pstmt.setString(1, key);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("JSON");
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Deletes all Oncotator Records.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public static void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE oncotator");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}