<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<%
    String siteTitle = SkinUtil.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);

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
    var dataPriority = <%=dataPriority%>;
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

            <div id="histogram_container"></div>
            <div id="oncoprint_key_container"></div>
            <div id="results_container"></div>

        </td>
    </tr>
    <tr>
        <td>
            <jsp:include page="global/footer.jsp" flush="true"/>
        </td>
    </tr>
</table>

<!-- templates -->
<script type="text/template" id="oncoprint_tmpl">
    <div id="oncoprint_body_{{id}}">
    </div>
</script>

<script type="text/template" id="histogram_control_divide_all_tmpl">
    <option value="1">Show percent of altered cases (studies with mutation data)</option>
    <option value="2">Show percent of altered cases (studies without mutation data)</option>
    <option value="3">Show number of altered cases (studies with mutation data)</option>
    <option value="4">Show number of altered cases (studies without mutation data)</option>
</script>

<script type="text/template" id="histogram_control_mut_tmpl">
    <option value="1">Show percent of altered cases (studies with mutation data)</option>
    <option value="3">Show number of altered cases (studies with mutation data)</option>
</script>

<script type="text/template" id="histogram_control_all_tmpl">
    <option value="1">Show percent of altered cases</option>
    <option value="3">Show number of altered cases</option>
</script>


<script type="text/template" id="histogram_tmpl">
    <div id="historam_toggle" style="text-align: right; padding-right: 125px">
        <select id="hist_toggle_box">
        </select>
        |
        <a href="#" id="histogram_sort" title="Sorts/unsorts histograms by alteration in descending order">Sort</a>
        |
        <a href="#" id="download_histogram" title="Downloads the current histogram in SVG format.">Export</a>
    </div>
    <div id="chart_div1" style="width: 975px; height: 450px;"></div>
    <div id="chart_div2" style="width: 975px; height: 450px;"></div>
    <div id="chart_div3" style="width: 975px; height: 450px;"></div>
    <div id="chart_div4" style="width: 975px; height: 450px;"></div>
</script>

<script type="text/template" id="oncoprint_key_tmpl">
    <div id="oncoprint_key">
        <svg id="cna" style="display:none;width:280px;" height=40>
            <g transform="translate(0,10)">
                <rect fill="#FF0000" width="5.5" height="23"></rect>
                <text x=10 y=16>Amplification</text>
            </g>
            <g transform="translate(100,10)">
                <rect fill="#0000FF" width="5.5" height="23"></rect>
                <text x=10 y=16>Homozygous deletion</text>
            </g>
        </svg>

        <svg id="mrna" style="display:none;width:330px;" height=40>
            <g transform="translate(0,10)">
                <rect class="cna" fill="#D3D3D3" width="5.5" height="23"></rect>
                <rect fill="none" stroke-width="1" stroke-opacity="1" width="5.5" height="23" stroke="#FF9999"></rect>
                <text x=10 y=16>MRNA Upregulated</text>
            </g>
            <g transform="translate(140,10)">
                <rect class="cna" fill="#D3D3D3" width="5.5" height="23"></rect>
                <rect class="mrna" fill="none" stroke-width="1" stroke-opacity="1" width="5.5" height="23" stroke="#6699CC"></rect>
                <text x=10 y=16>MRNA Downregulated</text>
            </g>
        </svg>

        <svg id="rppa" style="display:none;width:330px;" height=40>
            <rect fill="#D3D3D3" width="5.5" height="23"></rect>
            <path fill="#000000" d="M 0 7.666666666666667 l 2.75 -7.666666666666667 l 2.75 7.666666666666667 l 0 0"></path>
            </g>

            <g transform="translate(0,10)">
                <rect class="cna" fill="#D3D3D3" width="5.5" height="23"></rect>
                <rect class="mrna" fill="none" stroke-width="1" stroke-opacity="1" width="5.5" height="23" stroke="#FF9999"></rect>
                <text x=10 y=16>RPPA Upregulated</text>
            </g>

            <g transform="translate(135,10)">
                <rect fill="#D3D3D3" width="5.5" height="23"></rect>
                <path fill="#000000" d="M 0 15 l 2.75 7.666666666666667 l 2.75 -7.666666666666667 l 0 0"></path>
                <text x=10 y=16>RPPA Downregulated</text>
            </g>
        </svg>

        <svg id="mutation" style="display:none;" width=150 height=40>
            <g transform="translate(0,10)">
                <rect fill="#D3D3D3" width="5.5" height="23"></rect>
                <rect fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>
                <text x=10 y=16>Mutation</text>
            </g>
        </svg>
    </div>
</script>
<!-- end of templates -->

<script type="text/javascript" src="js/d3.v2.min.js"></script>
<script type="text/javascript" src="js/oncoprint.js"></script>
<script type="text/javascript" src="js/MemoSort.js"></script>
<script type="text/javascript" src="js/nv.d3.js"></script>
<script type="text/javascript" src="js/crosscancer.js"></script>

<jsp:include page="global/xdebug.jsp" flush="true"/>
</body>
</html>
