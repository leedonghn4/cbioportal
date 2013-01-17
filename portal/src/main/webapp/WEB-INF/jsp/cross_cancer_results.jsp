<%@ page import="org.mskcc.cbio.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>
<%@ page import="java.io.IOException" %>

<%
    String siteTitle = SkinUtil.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);

    // Get priority settings
    Integer dataPriority;
    try {
        dataPriority
                = Integer.parseInt(request.getParameter(QueryBuilder.DATA_PRIORITY).trim());
    } catch (Exception e) {
        dataPriority = 0;
    }

    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST).replace("\n", " ");
%>

<jsp:include page="global/header.jsp" flush="true"/>

<script type="text/javascript">
    var geneList = "<%=geneList%>";
</script>

<table width="100%">
    <tr>
        <td>
            <p>
                <a href=""
                      title="Modify your original query.  Recommended over hitting your browser's back button."
                      id="toggle_query_form">
                    <span class='query-toggle ui-icon ui-icon-triangle-1-e'
                          style='float:left;'></span>
                    <span class='query-toggle ui-icon ui-icon-triangle-1-s'
                          style='float:left; display:none;'></span><b>Modify Query</b>
                </a>
            <p/>

            <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
                <%@ include file="query_form.jsp" %>
            </div>

            <hr>

            <div id="results_container">
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <jsp:include page="global/footer.jsp" flush="true"/>
        </td>
    </tr>
</table>
</center>
</div>

<script type="text/javascript" src="js/nv.d3.js"></script>
<script type="text/javascript" src="js/crosscancer.js"></script>

<jsp:include page="global/xdebug.jsp" flush="true"/>
</body>
</html>
