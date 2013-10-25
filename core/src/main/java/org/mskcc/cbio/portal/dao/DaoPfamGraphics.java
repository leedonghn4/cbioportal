/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Dao for the pfam graphics cache.
 *
 * @author Selcuk Onur Sumer
 */
public class DaoPfamGraphics
{
	/**
	 * Inserts the given key and text pair to the database.
	 *
	 * @param uniprotId	uniprot id
	 * @param jsonData	pfam graphics data as a JSON object
	 * @throws DaoException	if an entity already exists with the same key
	 */
	public int addPfamGraphics(String uniprotId, String jsonData) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = JdbcUtil.getDbConnection(DaoPfamGraphics.class);
			pstmt = con.prepareStatement(
				"INSERT INTO pfam_graphics (`UNIPROT_ID`, `JSON_DATA`) VALUES (?,?)");
			pstmt.setString(1, uniprotId);
			pstmt.setString(2, jsonData);

			int rows = pstmt.executeUpdate();

			return rows;
		}
		catch (SQLException e)
		{
			throw new DaoException(e);
		}
		finally
		{
			JdbcUtil.closeAll(DaoPfamGraphics.class, con, pstmt, rs);
		}
	}

	/**
	 * Retrieves the text corresponding to the given key form the DB.
	 *
	 * @param uniprotId	a uniprot id
	 * @return  pfam data as a JSON string
	 * @throws DaoException
	 */
	public String getPfamGraphics(String uniprotId) throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = JdbcUtil.getDbConnection(DaoPfamGraphics.class);
			pstmt = con.prepareStatement(
					"SELECT * FROM pfam_graphics WHERE UNIPROT_ID=?");
			pstmt.setString(1, uniprotId);
			rs = pstmt.executeQuery();

			if (rs.next())
			{
				return rs.getString("JSON_DATA");
			}

			return null;
		}
		catch (SQLException e)
		{
			throw new DaoException(e);
		}
		finally
		{
			JdbcUtil.closeAll(DaoPfamGraphics.class, con, pstmt, rs);
		}
	}
        
        public static Map<String,String> getAllPfamGraphics()  throws DaoException
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			con = JdbcUtil.getDbConnection(DaoPfamGraphics.class);
			pstmt = con.prepareStatement(
					"SELECT * FROM pfam_graphics");
			rs = pstmt.executeQuery();

                        Map<String,String> map = new HashMap<String,String>();
			while (rs.next())
			{
				map.put(rs.getString("UNIPROT_ID"), rs.getString("JSON_DATA"));
			}

			return map;
		}
		catch (SQLException e)
		{
			throw new DaoException(e);
		}
		finally
		{
			JdbcUtil.closeAll(DaoPfamGraphics.class, con, pstmt, rs);
		}
	}
}
