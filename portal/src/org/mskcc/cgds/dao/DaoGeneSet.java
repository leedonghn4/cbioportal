package org.mskcc.cgds.dao;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.GeneSet;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for the Gene Set Table.
 */
public class DaoGeneSet {
    private final static String DELIM = ":";

    /**
     * Add a new gene set.
     * @param name          Name.
     * @param description   Description.
     * @param geneList      Gene List.
     * @return number of records added.
     * @throws DaoException Database Error.
     */
    public int addGeneSet (String name, String description, ArrayList<CanonicalGene> geneList)
        throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement
                    ("INSERT INTO gene_set (`NAME`, `DESCRIPTION`, `GENE_LIST`) " +
                            "VALUES (?,?,?)");
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, getGeneListString(geneList));
            int rows = pstmt.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    /**
     * Gets an ArrayList of all Gene Sets.
     * @return ArrayList of GeneSet Objects.
     * @throws DaoException Database Error.
     */
    public ArrayList<GeneSet> getAllGeneSets() throws DaoException {
        ArrayList<GeneSet> geneSetList = new ArrayList<GeneSet>();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();

            pstmt = con.prepareStatement
                    ("SELECT * FROM gene_set");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                GeneSet geneSet = extractGeneSet(rs);
                geneSetList.add(geneSet);
            }
            return geneSetList;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }

    private GeneSet extractGeneSet(ResultSet rs) throws SQLException, DaoException {
        GeneSet geneSet = new GeneSet();
        geneSet.setName(rs.getString("NAME"));
        geneSet.setDescription(rs.getString("DESCRIPTION"));
        String geneListStr = rs.getString("GENE_LIST");
        ArrayList<CanonicalGene> geneList = getGeneList(geneListStr);
        geneSet.setGeneList(geneList);
        return geneSet;
    }

    private ArrayList<CanonicalGene> getGeneList(String geneListStr) throws DaoException {
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        String entrezIds[] = geneListStr.split(DELIM);
        for (String currentId:  entrezIds) {
            Long currentEntrezId = Long.parseLong(currentId);
            CanonicalGene gene = daoGeneOptimized.getGene(currentEntrezId);
            geneList.add(gene);
        }
        return geneList;
    }

    private String getGeneListString(ArrayList<CanonicalGene> geneList) {
        StringBuffer buf = new StringBuffer();
        for (CanonicalGene currentGene:  geneList) {
            buf.append(currentGene.getEntrezGeneId() + DELIM);
        }
        return buf.toString();
    }

    public void deleteAllRecords() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection();
            pstmt = con.prepareStatement("TRUNCATE TABLE gene_set");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(con, pstmt, rs);
        }
    }
}