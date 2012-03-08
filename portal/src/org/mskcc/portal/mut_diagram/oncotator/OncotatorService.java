package org.mskcc.portal.mut_diagram.oncotator;

import org.mskcc.cgds.dao.DaoOncotatorCache;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.portal.util.WebFileConnect;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Connects to OncotatorRecord and Retrieves Details on a Single Mutation.
 */
public class OncotatorService {
    private static OncotatorService oncotatorService;
    private HashMap<String, OncotatorRecord> cache = new HashMap<String, OncotatorRecord>();
    private static final Logger logger = Logger.getLogger(OncotatorService.class);
    
    private OncotatorService () {
    }
    
    public static OncotatorService getInstance() {
        if (oncotatorService == null) {
            oncotatorService = new OncotatorService();
        }
        return oncotatorService;
    }

    public OncotatorRecord getOncotatorAnnotation(String chr, long start, long end, String referenceAllele,
            String observedAllele) throws DaoException, IOException {
        String key = createKey(chr, start, end, referenceAllele, observedAllele);

        //  First Check Cache
        String json = DaoOncotatorCache.getOncotatorRecord(key);
        if (json != null) {
            logger.warn("Got data from DB Cache");
            OncotatorRecord oncotatorRecord = OncotatorParser.parseJSON(key, json);
            return oncotatorRecord;
        } else {
            //  Otherwise, connect to OncotatorRecord
            URL url = new URL("http://www.broadinstitute.org/oncotator/mutation/" + key);
            logger.warn("Getting live data:  " + url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String content = WebFileConnect.readFile(in);
            if (content != null) {
                OncotatorRecord oncotatorRecord = OncotatorParser.parseJSON(key, content);
                
                //  Store JSON to DB Cache
                DaoOncotatorCache.addOncotatorRecord(key, content);
                return oncotatorRecord;
            } else {
                return null;
            }
        }
    }

    public static String createKey(String chr, long start, long end, String referenceAllele,
            String observedAllele) {
        return chr + "_" + start + "_" + end + "_" + referenceAllele + "_" + observedAllele;
    }
}