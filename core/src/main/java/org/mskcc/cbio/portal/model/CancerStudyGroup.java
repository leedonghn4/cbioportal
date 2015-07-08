/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.GetGeneticProfiles;

/**
 *
 * @author dongli
 */
public class CancerStudyGroup {




    /**
     * NO_SUCH_STUDY Internal ID has not been assigned yet.
     */
    public static final int NO_SUCH_STUDY = -1;

    private int studyGroupID; // assigned by dbms auto increment
    private String studyGroupName;
    private String cancerStudyGroupIdentifier;

    

    /**
     * Constructor.
     * @param name                  Name of Cancer Study.
     * @param description           Description of Cancer Study.
     * @param cancerStudyIdentifier Cancer Study Stable Identifier.
     * @param typeOfCancerId        Type of Cancer.
     * @param publicStudy           Flag to indicate if this is a public study.
     */
    public CancerStudyGroup(String studyGroupName,String cancerStudyGroupIdentifier) {
        this.studyGroupID = CancerStudyGroup.NO_SUCH_STUDY;
        this.studyGroupName = studyGroupName;
        this.cancerStudyGroupIdentifier = cancerStudyGroupIdentifier;
    }
  
    /**
     * Gets the Cancer Study Stable Identifier.
     * @return cancer study stable identifier.
     */
    public int getInternalId() {
        return studyGroupID;
    }
    /**
     * Sets the Internal ID associated with this record.
     * @param studyId internal integer ID.
     */
    public void setInternalId(int studygroupId) {
        this.studyGroupID = studygroupId;
    }
    /**
     * Gets the Cancer Study Group Identifier.
     * @return cancer study Group Identifier.
     */
    public String getCancerStudyGroupIdentifier() {
        return cancerStudyGroupIdentifier;
    }
    /**
     * Sets the Internal ID associated with this record.
     * @param studyId internal integer ID.
     */
    public void setCancerStudyGroupIdentifier(String cancerStudyGroupIdentifier) {
        this.cancerStudyGroupIdentifier = cancerStudyGroupIdentifier;
    }
    
    /**
     * Gets the Cancer Study Group Identifier.
     * @return cancer study Group Identifier.
     */
    public String getCancerStudyGroupName() {
        return studyGroupName;
    }
    /**
     * Gets the Cancer Study Group Identifier.
     * @return cancer study Group Identifier.
     */
    public void setCancerStudyGroupName(String studyGroupName) {
        this.studyGroupName = studyGroupName;
    }
    
    

}
