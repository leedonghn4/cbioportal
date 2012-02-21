package org.mskcc.portal.mut_diagram.oncotator;

import java.util.HashMap;

/**
 * Connects to Oncotator and Retrieves Details on a Single Mutation.
 */
public class OncotatorService {
    private static OncotatorService oncotatorService;
    private HashMap<String, Oncotator> cache = new HashMap<String, Oncotator>();
    
    private OncotatorService () {
    }
    
    public static OncotatorService getInstance() {
        if (oncotatorService == null) {
            oncotatorService = new OncotatorService();
        }
        return oncotatorService;
    }

}
