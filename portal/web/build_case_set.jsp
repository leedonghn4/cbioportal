<%@ page import="org.mskcc.cgds.dao.DaoClinicalFreeForm" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.dao.DaoCancerStudy" %>
<%@ page import="org.mskcc.cgds.model.ClinicalParameterMap" %>
<%@ page import="java.util.HashSet" %>

<html>
<body>
<h1>Build a Case Set</h1>

<div class="query_step_section" id="step3">
<form action="build_case_set_execute.jsp" METHOD="GET"> 
<%
    CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("ucec_tcga ");

    DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
    HashSet<String> clinicalCaseSet = daoClinicalFreeForm.getAllCases(cancerStudy.getInternalId());
    out.println ("<b>Total Number of Cases in Cancer Study:  " + clinicalCaseSet.size() + "</b><P>");

    HashSet<String> paramSet = daoClinicalFreeForm.getDistinctParameters(cancerStudy.getInternalId());
    out.println("<table>");
    for (String param : paramSet) {
        ClinicalParameterMap paramMap = daoClinicalFreeForm.getDataSlice(cancerStudy.getInternalId(), param);
        HashSet<String> distinctCategorySet = paramMap.getDistinctCategories();
        if (distinctCategorySet.size() < 10) {
            out.println("<tr valign=top>");
            out.println("<td><input type='checkbox' name='clinical_check_" + param + "'/></td>");
            out.println("<td> " + param + ":</td>");
            out.println("<td>");
            if (distinctCategorySet.size() == 2) {
                out.println("<select name='clinical_" + param + "'>");
                for (String category : distinctCategorySet) {
                    out.println("<option value='" + category + "'>" + category + "</option>");
                }
                out.println("</select>");
            } else {
                for (String category : distinctCategorySet) {
                    if (category.trim().length() > 0) {
                        out.println("<input type='checkbox' name='clinical_subcheck_" + param
                                + "' value='" + category + "'>");
                        out.println(category + "</input><br>");
                    }
                }
            }
            out.println("</td>");
            out.println("</tr>");
        }
    }
    out.println("</table>");
%>

<input TYPE="SUBMIT" VALUE="Build it!">

</form>
</div>
</body>
</html>