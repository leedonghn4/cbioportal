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
 * Connects to Oncotator and Retrieves Details on a Single Mutation.
 */
public class OncotatorService {
    private static OncotatorService oncotatorService;
    private HashMap<String, Oncotator> cache = new HashMap<String, Oncotator>();
    private static final Logger logger = Logger.getLogger(OncotatorService.class);
    
    private OncotatorService () {
    }
    
    public static OncotatorService getInstance() {
        if (oncotatorService == null) {
            oncotatorService = new OncotatorService();
        }
        return oncotatorService;
    }

    public Oncotator getOncotatorAnnotation(String chr, long start, long end, String referenceAllele,
            String observedAllele) throws DaoException, IOException {
        String key = createKey(chr, start, end, referenceAllele, observedAllele);

        //  First Check Cache
        String json = DaoOncotatorCache.getOncotatorRecord(key);
        if (json != null) {
            logger.warn("Got data from DB Cache");
            Oncotator oncotator = OncotatorParser.parseJSON(json);
            return oncotator;
        } else {
            //  Otherwise, connect to Oncotator
            URL url = new URL("http://www.broadinstitute.org/oncotator/mutation/" + key);
            logger.warn("Getting live data:  " + url);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String content = WebFileConnect.readFile(in);
            if (content != null) {
                Oncotator oncotator = OncotatorParser.parseJSON(content);
                
                //  Store JSON to DB Cache
                DaoOncotatorCache.addOncotatorRecord(key, content);
                return oncotator;
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