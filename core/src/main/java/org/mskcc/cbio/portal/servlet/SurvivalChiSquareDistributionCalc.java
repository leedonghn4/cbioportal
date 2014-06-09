/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Calculate p-value from chi square score
 * (degree of freedom is fixed as 1)
 *
 *
 * @author : yichao S
 * @date: Sep 2013
 * @param chi_square_score
 *
 * @return p_value
 */

public class SurvivalChiSquareDistributionCalc extends HttpServlet {

    /**
     * Handles HTTP GET Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws javax.servlet.ServletException
     */
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    /**
     * Handles the HTTP POST Request.
     *
     * @param httpServletRequest  HttpServletRequest
     * @param httpServletResponse HttpServletResponse
     * @throws ServletException
     */
    protected void doPost(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        
        
        if(httpServletRequest.getParameterMap().containsKey("chi_square_score")) {
            String chi_square_score = httpServletRequest.getParameter("chi_square_score");
            ChiSquaredDistribution distribution;
            distribution = new ChiSquaredDistribution(1);
            double p_value = 1 - distribution.cumulativeProbability(Float.parseFloat(chi_square_score));
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.write(Double.toString(p_value));
        }else if(httpServletRequest.getParameterMap().containsKey("chi_square_score_list")) {
            String scoreString = httpServletRequest.getParameter("chi_square_score_list");
            String jobKey = httpServletRequest.getParameter("job_key");
            Pattern p = Pattern.compile("[,\\s]+");
            String scoreList[] = p.split(scoreString);
            int scoreListLength = scoreList.length;
            String pvalueList = "";
            for (int i = 0; i < scoreListLength; i++) {
                String selectedScore = scoreList[i];
                selectedScore = selectedScore.trim();
                float score = Float.parseFloat(selectedScore);
                if(Float.isNaN(score)) {
                    pvalueList += ",";
                }else {
                    ChiSquaredDistribution distribution = new ChiSquaredDistribution(1);
                    double p_value = 1 - distribution.cumulativeProbability(score);
                    pvalueList += Double.toString(p_value) + ",";
                }
            }
            pvalueList = pvalueList.substring(0, pvalueList.length()-1);
            
            Map obj = new HashMap();
            obj.put("jobKey", jobKey);
            obj.put("pvalueList", pvalueList);
            ObjectMapper mapper = new ObjectMapper();
            httpServletResponse.setContentType("application/json");
            PrintWriter out = httpServletResponse.getWriter();
            out.write(mapper.writeValueAsString(obj));
        }else {
            httpServletResponse.setContentType("text/plain");
            PrintWriter out = httpServletResponse.getWriter();
            out.write("Didn't find related command function.");
        }
    }
}