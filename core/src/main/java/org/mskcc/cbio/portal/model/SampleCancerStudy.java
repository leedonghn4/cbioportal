/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.model;

/**
 *
 * @author dongli
 */
public class SampleCancerStudy {

    
    private int internalId;
    private int cancerStudyId;

    public SampleCancerStudy(int internalId, int cancerStudyId)
    {
        this.cancerStudyId = cancerStudyId;
        this.internalId = internalId;
    }

    public int getInternalId()
    {
        return internalId;
    }

    public int getCancerStudyId()
    {
        return cancerStudyId;
    }  
}
