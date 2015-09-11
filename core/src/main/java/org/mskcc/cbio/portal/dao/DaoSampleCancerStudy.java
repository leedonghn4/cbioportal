/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.apache.commons.collections.map.MultiKeyMap;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author dongli
 */
public class DaoSampleCancerStudy {

    private static final int MISSING_CANCER_STUDY_ID = -1;

    private static final Map<Integer, Integer> byInternalId = new ConcurrentHashMap<Integer, Integer>();
    private static final Map<Integer, Integer> byCancerStudyId = new HashMap<Integer, Integer>();

    static {
        reCache();
    }

    private static void clearCache()
    {
        byInternalId.clear();
        byCancerStudyId.clear();
    }

    public static synchronized void reCache()
    {
        clearCache();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleCancerStudy.class);
            pstmt = con.prepareStatement("SELECT * FROM sample_cancerstudy");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                cacheSample(extractSampleCancerStudy(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            JdbcUtil.closeAll(DaoSampleCancerStudy.class, con, pstmt, rs);
        }
    }

    private static void cacheSampleCancerStudy(SampleCancerStudy samplecancerstudy)
    {
        cacheSampleCancerStudy(samplecancerstudy, getCancerStudyId(samplecancerstudy));
    }

    private static int getCancerStudyId(Sample sample)
    {
        Patient patient = DaoPatient.getPatientById(sample.getInternalPatientId());
        return (patient == null) ? MISSING_CANCER_STUDY_ID : patient.getCancerStudy().getInternalId();
    }

    private static void cacheSample(Sample sample, int cancerStudyId)
    {
        if (!byStableId.containsKey(sample.getStableId())) {
            byStableId.put(sample.getStableId(), sample);
        }

        if (!byInternalId.containsKey(sample.getInternalId())) {
            byInternalId.put(sample.getInternalId(), sample);
        }

        Map<String, Sample> samples = byInternalPatientAndStableSampleId.get(sample.getInternalPatientId());
        if (samples==null) {
            samples = new HashMap<String, Sample>();
            byInternalPatientAndStableSampleId.put(sample.getInternalPatientId(), samples);
        }
        if (samples.containsKey(sample.getStableId())) {
            System.err.println("Something is wrong: there are two samples of "+sample.getStableId()+" in the same patient.");
        }
        samples.put(sample.getStableId(), sample);

        samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
            samples = new HashMap<String, Sample>();
            byCancerStudyIdAndStableSampleId.put(cancerStudyId, samples);
        }
        if (samples.containsKey(sample.getStableId())) {
            System.err.println("Something is wrong: there are two samples of "+sample.getStableId()+" in the same study.");
        }
        samples.put(sample.getStableId(), sample);
    }

    public static int addSampleCancerStudy(SampleCancerStudy sample) throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSample.class);
            pstmt = con.prepareStatement("INSERT INTO sample " +
                                         "( `STABLE_INTERNAL_ID`, `SAMPLE_TYPE`, `PATIENT_ID`, `TYPE_OF_CANCER_ID` ) " +
                                         "VALUES (?,?,?,?)",
                                         Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, sample.getStableId());
            pstmt.setString(2, sample.getType().toString());
            pstmt.setInt(3, sample.getInternalPatientId());
            pstmt.setString(4, sample.getCancerTypeId());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cacheSample(new SampleCancerStudy(rs.getInt(1), sample.getStableId(),
                                       sample.getInternalPatientId(), sample.getCancerTypeId()));
                return rs.getInt(1);
            }
            return -1;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
        }
    }

        public static List<int> getAllSamplesId()
    {
        return (byStableId.isEmpty()) ? Collections.<int>emptyList() :
            new ArrayList<int>(byStableId.values());
    }
    
    public static List<Sample> getAllSamples()
    {
        return (byStableId.isEmpty()) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byStableId.values());
    }

    public static Sample getSampleById(int internalId)
    {
        return byInternalId.get(internalId);
    }

    public static List<Sample> getSamplesByPatientId(int internalPatientId)
    {
        return (byInternalPatientAndStableSampleId.isEmpty() || !byInternalPatientAndStableSampleId.containsKey(internalPatientId)) ? Collections.<Sample>emptyList() :
            new ArrayList<Sample>(byInternalPatientAndStableSampleId.get(internalPatientId).values());
    }

    public static Sample getSampleByPatientAndSampleId(int internalPatientId, String stableSampleId)
    {
        Map<String, Sample> samples = byInternalPatientAndStableSampleId.get(internalPatientId);
        if (samples==null) {
            return null;
        }
        
        return samples.get(stableSampleId);
    }
    
    public static List<Sample> getSamplesByCancerStudy(int cancerStudyId)
    {
        Map<String, Sample> samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
            return Collections.emptyList();
        }
        
        return new ArrayList<Sample>(samples.values());
    }

    public static Sample getSampleByCancerStudyAndSampleId(int cancerStudyId, String stableSampleId)
    {
        Map<String, Sample> samples = byCancerStudyIdAndStableSampleId.get(cancerStudyId);
        if (samples==null) {
            return null;
        }
        
        return samples.get(stableSampleId);
    }

    public static void deleteAllRecords() throws DaoException
    {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoSampleCancerStudy.class);
            pstmt = con.prepareStatement("TRUNCATE TABLE sample_cancerstudy");
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoSampleCancerStudy.class, con, pstmt, rs);
        }

        clearCache();
    }

    private static SampleCancerStudy extractSampleCancerStudy(ResultSet rs) throws SQLException
    {
        return new SampleCancerStudy(rs.getInt("SAMPLE_INTERNAL_ID"),
                          rs.getInt("CANCER_STUDY_ID"));
    }
    
}
