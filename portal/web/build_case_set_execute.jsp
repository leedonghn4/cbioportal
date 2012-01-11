<%@ page import="org.mskcc.cgds.dao.DaoClinicalFreeForm" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.dao.DaoCancerStudy" %>
<%@ page import="org.mskcc.cgds.model.ClinicalParameterMap" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.mskcc.portal.model.CaseFilter" %>
<%@ page import="org.mskcc.cgds.dao.DaoException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.net.URLDecoder" %>

<html>
<body>
<h1>Here is your custom case set:</h1>

<%
    CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("ucec_tcga ");
    DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();

    Enumeration paramEnumerator = request.getParameterNames();
    HashSet<String> checkboxSet = new HashSet<String>();
    HashMap<String, HashSet<String>> valuesSelected = new HashMap<String, HashSet<String>>();

    //  Print Everything Out
    while (paramEnumerator.hasMoreElements()) {
        String param = (String) paramEnumerator.nextElement();
        //out.println(param + ": " + request.getParameter(param) + "<br>");
    }

    //  Extract Checkboxes
    paramEnumerator = request.getParameterNames();
    while (paramEnumerator.hasMoreElements()) {
        String param = (String) paramEnumerator.nextElement();
        if (param.startsWith("clinical_check_")) {
            String paramNameOriginal = param.replaceAll("clinical_check_", "");
            // out.println ("Adding checkbox:  " + paramNameOriginal + "<BR>");
            checkboxSet.add(paramNameOriginal);
        }
    }

    HashMap<String, HashSet<String>> valueMap = new HashMap<String, HashSet<String>>();

    //  Extract Filter Values
    paramEnumerator = request.getParameterNames();
    while (paramEnumerator.hasMoreElements()) {
        String param = (String) paramEnumerator.nextElement();
        if (param.startsWith("clinical_check_")) {
            // Ignore
        } else if (param.startsWith("clinical_subcheck_")) {
            String paramNameOriginal = param.replaceAll("clinical_subcheck_", "");
            String values[] = request.getParameterValues(param);
            // out.println("Evaluating:  " + paramNameOriginal + ": " + value + " --> ");
            if (checkboxSet.contains(paramNameOriginal)) {
                for (String value:  values) {
                    updateValueMap(valueMap, paramNameOriginal, value);
                }
            } else {
                // out.println("Not adding.<BR>");
            }
        } else if (param.startsWith("clinical_")) {
            String paramNameOriginal = param.replaceAll("clinical_", "");
            // out.println("Evaluating:  " + paramNameOriginal + " ");
            if (checkboxSet.contains(paramNameOriginal)) {
                String values[] = request.getParameterValues(param);
                for (String value:  values) {
                    out.println("Adding Filter:  " + paramNameOriginal + ":  " + value + "<BR>");
                    updateValueMap(valueMap, paramNameOriginal, value);
                }
            } else {
                // out.println("Not adding.<BR>");
            }
        }
    }

    ArrayList<CaseFilter> filterList = new ArrayList<CaseFilter>();
    for (String param:  valueMap.keySet()) {
        filterList.add(new CaseFilter(param, valueMap.get(param)));
    }

    HashSet<String> caseSet = filterCases(cancerStudy.getInternalId(), filterList, out);
    out.println("Final Case Set:  " + caseSet.size());
    out.println("<P>");
    for (String caseId:  caseSet) {
        out.println (caseId + "<BR>");
    }
%>

<%!
    public HashSet<String> filterCases(int cancerStudyId, ArrayList<CaseFilter> filterList, JspWriter out) throws
            DaoException, IOException {
        DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
        ArrayList<HashSet<String>> filteredCaseSets = new ArrayList<HashSet<String>>();
        HashSet<String> allCases = daoClinicalFreeForm.getAllCases(cancerStudyId);
        for (CaseFilter filter : filterList) {
            HashSet<String> currentCaseSet = new HashSet<String>();
            String paramName = filter.getParamName();
            HashSet<String> paramValueSet = filter.getParamValueSet();
            ClinicalParameterMap clinicalMap = daoClinicalFreeForm.getDataSlice(cancerStudyId, paramName);
            for (String caseId : allCases) {
                String currentValue = clinicalMap.getValue(caseId);
                if (paramValueSet.contains(currentValue)) {
                    currentCaseSet.add(caseId);
                }
            }
            out.println(filter.toString() + "--> " + currentCaseSet.size() + "<BR>");
            filteredCaseSets.add(currentCaseSet);
        }

        HashSet<String> filteredCaseSet = new HashSet<String>(allCases);
        for (HashSet<String> currentCaseSet : filteredCaseSets) {
            filteredCaseSet.retainAll(currentCaseSet);
        }
        return filteredCaseSet;
    }

    private void updateValueMap(HashMap<String, HashSet<String>> valueMap, String name, String value) {
        HashSet<String> valueSet = new HashSet<String>();
        if (valueMap.containsKey(name)) {
            valueSet = valueMap.get(name);
            valueMap.put(name, valueSet);
        }
        valueSet.add(value);
        valueMap.put(name, valueSet);
    }
%>

</body>
</html>