<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.DrugsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.IGVLinking" %>


<%
ServletXssUtil xssUtil = ServletXssUtil.getInstance();
ObjectMapper jsonMapper = new ObjectMapper();
boolean print = "1".equals(request.getParameter("print"));
boolean isPatientView = "patient".equals(request.getAttribute(PatientView.VIEW_TYPE));
request.setAttribute("tumormap", true);
List<String> caseIds = (List<String>)request.getAttribute(PatientView.SAMPLE_ID);
String jsonCaseIds = jsonMapper.writeValueAsString(caseIds);
String caseIdStr = StringUtils.join(caseIds," ");
String patientViewError = (String)request.getAttribute(PatientView.ERROR);
CancerStudy cancerStudy = (CancerStudy)request.getAttribute(PatientView.CANCER_STUDY);

// check if any Bam files exist
boolean viewBam = false;
Map<String,Boolean> mapCaseBam = new HashMap<String,Boolean>(caseIds.size());
for (String caseId : caseIds) {
    boolean exist = IGVLinking.bamExists(cancerStudy.getCancerStudyStableId(), caseId);
    mapCaseBam.put(caseId, exist);
    if (exist) {
        viewBam = true;
    }
}
String jsonMapCaseBam = jsonMapper.writeValueAsString(mapCaseBam);

String jsonClinicalData = jsonMapper.writeValueAsString((Map<String,String>)request.getAttribute(PatientView.CLINICAL_DATA));

String tissueImageUrl = (String)request.getAttribute(PatientView.TISSUE_IMAGES);
boolean showTissueImages = tissueImageUrl!=null;

String patientID = (String)request.getAttribute(PatientView.PATIENT_ID);
int numTumors = (Integer)request.getAttribute("num_tumors");

boolean showTimeline = (Boolean)request.getAttribute("has_timeline_data");

String pathReportUrl = (String)request.getAttribute(PatientView.PATH_REPORT_URL);

String oncokbUrl = (String)GlobalProperties.getOncoKBUrl();

//String drugType = xssUtil.getCleanerInput(request, "drug_type");
String drugType = request.getParameter("drug_type");

GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(PatientView.MUTATION_PROFILE);
boolean showMutations = mutationProfile!=null;

GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(PatientView.CNA_PROFILE);
boolean showCNA = cnaProfile!=null;

GeneticProfile mrnaProfile = (GeneticProfile)request.getAttribute(PatientView.MRNA_PROFILE);

String isDemoMode = request.getParameter("demo");
boolean showPlaceHoder;
if (isDemoMode!=null) {
    showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
} else {
    showPlaceHoder = GlobalProperties.showPlaceholderInPatientView();
}

boolean showPathways = showPlaceHoder & (showMutations | showCNA);
boolean showSimilarPatient = false;//showPlaceHoder & (showMutations | showCNA);

boolean hasCnaSegmentData = ((Boolean)request.getAttribute(PatientView.HAS_SEGMENT_DATA));
boolean hasAlleleFrequencyData =  ((Boolean)request.getAttribute(PatientView.HAS_ALLELE_FREQUENCY_DATA));
boolean showGenomicOverview = showMutations | hasCnaSegmentData;
boolean showClinicalTrials = true;
boolean showDrugs = true;

double[] genomicOverviewCopyNumberCnaCutoff = GlobalProperties.getPatientViewGenomicOverviewCnaCutoff();

int numPatientInSameStudy = 0;
int numPatientInSameMutationProfile = 0;
int numPatientInSameCnaProfile = 0;

boolean noData = cnaProfile==null & mutationProfile==null;

String mutationProfileStableId = null;
String cnaProfileStableId = null;
String mrnaProfileStableId = null;
if (mutationProfile!=null) {
    mutationProfileStableId = mutationProfile.getStableId();
}
if (cnaProfile!=null) {
    cnaProfileStableId = cnaProfile.getStableId();
}
if (mrnaProfile!=null) {
    mrnaProfileStableId = mrnaProfile.getStableId();
}

if (patientViewError!=null) {
    out.print(caseIdStr);
    out.print(": ");
    out.println();
    out.print(patientViewError);
} else {
    numPatientInSameStudy = (Integer)request.getAttribute(PatientView.NUM_CASES_IN_SAME_STUDY);
    if (mutationProfile!=null) {
        numPatientInSameMutationProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_MUTATION_PROFILE);
    }
    if (cnaProfile!=null) {
        numPatientInSameCnaProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_CNA_PROFILE);
    }
%>

<jsp:include page="../../global/header.jsp" flush="true" />

<%if(numTumors>1&&caseIds.size()==1) {%>
    <p style="background-color: lightyellow;"> This patient has 
        <a title="Go to multi-sample view" href="case.do?cancer_study_id=<%=cancerStudy.getCancerStudyStableId()%>&case_id=<%=patientID%>"><%=numTumors%> tumor samples</a>.
    </p>
<%}%>

<div id="clinical_div">
</div>

<div id="patient-tabs">
    <ul>
        
    <li><a id="link-summary" href='#tab_summary' class='patient-tab'>Summary</a></li>
    
    <%if(showMutations){%>
    <li><a id="link-mutations" href='#tab_mutations' class='patient-tab'>Mutations</a></li>
    <%}%>
    
    <%if(showCNA){%>
    <li><a id="link-cna" href='#tab_cna' class='patient-tab'>Copy Number Alterations</a></li>
    <%}%>

    <%if(showDrugs){%>
    <li><a id="link-drugs" href='#tab_drugs' class='patient-tab'>Drugs</a></li>
    <%}%>

    <%if(showClinicalTrials){%>
    <li><a id="link-clinical-trials" href='#tab_clinical-trials' class='patient-tab'>Clinical Trials</a></li>
    <%}%>
    
    <%if(showTissueImages){%>
    <li><a id="link-tissue-images" href='#tab_images' class='patient-tab'>Tissue Images</a></li>
    <%}%>
    
    <%if(pathReportUrl!=null){%>
    <li><a id="link-path-report" href='#tab_path-report' class='patient-tab'>Pathology Report</a></li>
    <%}%>

    <%if(showPathways){%>
    <li><a id="link-pathways" href='#tab_pathways' class='patient-tab'>Network</a></li>
    <%}%>
    
    <%if(showSimilarPatient){%>
    <li><a id="link-tissue-similar-patients" href='#tab_similar-patients' class='patient-tab'>Similar Patients</a></li>
    <%}%>

    </ul>

    <div class="patient-section" id="tab_summary">
        <%@ include file="summary.jsp" %>
    </div>

    <%if(showMutations){%>
    <div class="patient-section" id="tab_mutations">
        <%@ include file="mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNA){%>
    <div class="patient-section" id="tab_cna">
        <%@ include file="cna.jsp" %>
    </div>
    <%}%>

    <%if(showTissueImages){%>
    <div class="patient-section" id="tab_images">
        <%@ include file="tissue_images.jsp" %>
    </div>
    <%}%>

    <%if(pathReportUrl!=null){%>
    <div class="patient-section" id="tab_path-report">
        <%@ include file="path_report.jsp" %>
    </div>
    <%}%>

    <%if(showPathways){%>
    <div class="patient-section" id="tab_pathways">
        <%@ include file="pathways.jsp" %>
    </div>
    <%}%>

    <%if(showSimilarPatient){%>
    <div class="patient-section" id="tab_similar-patients">
        <%@ include file="similar_patients.jsp" %>
    </div>
    <%}%>

    <%if(showDrugs){%>
    <div class="patient-section" id="tab_drugs">
        <%@ include file="drugs.jsp" %>
    </div>
    <%}%>

    <%if(showClinicalTrials){%>
        <div class="patient-section" id="tab_clinical-trials">
            <%@ include file="clinical_trials.jsp" %>
        </div>
    <%}%>

</div>
<%  
}
%>
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="../../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="../../global/xdebug.jsp" flush="true" />

<link href="css/jquery.qtip.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
        @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_filter {
                width: 40%;
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                clear: none;
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
        .gene_mutation_percent_div {
                display: block;
                float: left;
                background-color: lightgreen;
                height: 12px;
        }
        .mutation_percent_div {
                display: block;
                float: left;
                background-color: green;
                height: 12px;
        }
        .amp_percent_div {
                display: block;
                float: left;
                background-color: red;
                height: 12px;
        }
        .del_percent_div {
                display: block;
                float: left;
                background-color: blue;
                height: 12px;
        }
        .left_float_div {
                display: block;
                float: left;
        }
        .right_float_div {
                display: block;
                float: right;
        }
        .qtip-wide {
            max-width: 600px;
        }
        .qtip {
            font-size: 11px;
        }
        .datatable-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
                vertical-align: middle;
        }
        .datatable-show-more {
            float: left;
        }
	.igv-link {
		cursor: pointer;
	}
</style>

<script type="text/javascript" src="js/src/patient-view/genomic-event-observer.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">

var print = <%=print%>;
var isPatientView = <%=isPatientView%>;
var placeHolder = <%=Boolean.toString(showPlaceHoder)%>;
var mutationProfileId = <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>';
var cnaProfileId = <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>';
var mrnaProfileId = <%=mrnaProfileStableId==null%>?null:'<%=mrnaProfileStableId%>';
var hasCnaSegmentData = <%=hasCnaSegmentData%>;
var hasAlleleFrequencyData = <%=hasAlleleFrequencyData%>;
var showGenomicOverview = <%=showGenomicOverview%>;
var caseIdsStr = '<%=caseIdStr%>';
var caseIds = <%=jsonCaseIds%>;
var patientId = '<%=patientID%>';
var cancerStudyName = "<%=cancerStudy.getName()%>";
var cancerStudyId = '<%=cancerStudy.getCancerStudyStableId()%>';
var genomicEventObs =  new GenomicEventObserver(<%=showMutations%>,<%=showCNA%>, hasCnaSegmentData);
var drugType = drugType?'<%=drugType%>':null;
var clinicalDataMap = <%=jsonClinicalData%>;
var viewBam = <%=viewBam%>;
var mapCaseBam = <%=jsonMapCaseBam%>;
var oncokbUrl = '<%=oncokbUrl%>';
var oncoKBDataReady = false;
    
var caseMetaData = {
    color : {}, label : {}, index : {}, tooltip : {}
};

$(document).ready(function(){
    if (print) $('#page_wrapper_table').css('width', '900px');
    tweaksStyles();
    outputClinicalData();
    setUpPatientTabs();
    initTabs();
    var openTab = /(tab_[^&]+)/.exec(window.location.hash);
    if (openTab) {
        switchToTab(openTab[1]);
    }
});

function tweaksStyles() {
    $("div#content").css("margin-top","0px");
    $("body").css("background-color", "#E0E0E0");
}

function setUpPatientTabs() {
    $('#patient-tabs').tabs();
    $('#patient-tabs').show();
    fixCytoscapeWebRedraw();
}

function initTabs() {
    var tabContainers = $('.patient-section');
    tabContainers.hide().filter(':first').show();

    $('.patient-tab').click(function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            $('.patient-tab').removeClass('selected');
            $(this).addClass('selected');
            return false;
    }).filter(':first').click();   
}

function fixCytoscapeWebRedraw() {
    // to initially hide the network tab
    $("#pathways").attr('style', 'display: none !important; height: 0px; width: 0px; visibility: hidden;');
    
    // to fix problem of flash repainting
    $("a.patient-tab").click(function(){
        if($(this).attr("href")==="#pathways") {
            $("#pathways").removeAttr('style');
        } else {
            $("#pathways").attr('style', 'display: block !important; height: 0px; width: 0px; visibility: hidden;');
        }
    });
}

function switchToTab(toTab) {
    $('.patient-section').hide();
    $('.patient-section#'+toTab).show();
    $('#patient-tabs').tabs("option",
		"active",
		$('#patient-tabs ul a[href="#'+toTab+'"]').parent().index());
    if (toTab==='images') {
        loadImages();
    }
}

function getEventString(eventTableData,dataCol,overviewCol) {
    var s = [];
    for (var i=0; i<eventTableData.length; i++) {
        if (overviewCol==null || eventTableData[i][overviewCol])
            s.push(eventTableData[i][dataCol]);
    }
    return s.join(",");
}

function getEventIndexMap(eventTableData,idCol) {
    var m = {};
    for (var i=0; i<eventTableData.length; i++) {
        m[eventTableData[i][idCol]] = i;
    }
    return m;
}
    
function addNoteTooltip(elem, content, position) {
    $(elem).qtip({
        content: (cbio.util.checkNullOrUndefined(content) ? {attr: 'alt'} : content),
	    show: {event: "mouseover"},
        hide: {fixed: true, delay: 100, event: "mouseout"},
        style: { classes: 'qtip-light qtip-rounded' },
        position: (cbio.util.checkNullOrUndefined(position) ? {my:'top left',at:'bottom center',viewport: $(window)} : position)
    });
}

function addMoreClinicalTooltip(elem) {
    $(elem).each(function( index ) {
        var thisElem = $(this);
        var caseId = thisElem.attr('alt');
        
        var clinicalData = [];
        for (var key in clinicalDataMap[caseId]) {
            clinicalData.push([key, clinicalDataMap[caseId][key]]);
        }

        if (clinicalData.length===0) {
            thisElem.remove();
        } else {
            thisElem.qtip({
                content: {
                    text: '<table id="more-clinical-table-'+caseId+'"></table>'
                },
                events: {
                    render: function(event, api) {
                        $(this).html("<table></table>");
                        $(this).find("table").dataTable( {
                            "sDom": 't',
                            "bJQueryUI": true,
                            "bDestroy": true,
                            "aaData": clinicalData,
                            "aoColumnDefs":[
                                {
                                    "aTargets": [ 0 ],
                                    "sClass": "left-align-td",
                                    "mRender": function ( data, type, full ) {
                                        return '<b>'+data+'</b>';
                                    }
                                },
                                {
                                    "aTargets": [ 1 ],
                                    "sClass": "left-align-td",
                                    "bSortable": false
                                }
                            ],
                            "aaSorting": [[0,'asc']],
                            "oLanguage": {
                                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                                "sInfoFiltered": "",
                                "sLengthMenu": "Show _MENU_ per page"
                            },
                            "iDisplayLength": -1
                        } );
                    }
                },
                    show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"},
                style: { classes: 'qtip-light qtip-rounded qtip-wide' },
                position: {my:'top right',at:'bottom right',viewport: $(window)}
            });
        }
    });
}

function addDrugsTooltip(elem, my, at) {
    $(elem).each(function(){
        $(this).qtip({
            content: {
                text: '<img src="images/ajax-loader.gif"/>',
                ajax: {
                    url: 'drugs.json',
                    type: 'POST',
                    data: {<%=DrugsJSON.DRUG_IDS%>: $(this).attr('alt')},
                    success: function(drugs,status) {
                        var txt = [];
                        for (var i=0, n=drugs.length; i<n; i++) {
                            var drug = drugs[i];
                            var txtDrug = [];
                            if (drug[2]) {
                                txtDrug.push("Drug name:</b></td><td><b>"+drug[2]+"</b>");
                            }
                            if (drug[1]) {
                                txtDrug.push("Target:</b></td><td><b>"+drug[1]+"</b>");
                            }
                            if (drug[3]) {
                                txtDrug.push("Synonyms:</b></td><td>"+drug[3]);
                            }
                            if (drug[4]) {
                                txtDrug.push("FDA approved?</b></td><td>"+(drug[4]?"Yes":"No"));
                            }
//                            if (drug[5]) {
//                                txtDrug.push("Description:</b></td><td>"+drug[5]);
//                            }
//                            if (drug[7]) { // xref
//                                var xref = [];
//                                var nci = drug[7]['NCI_Drug'];
//                                if (nci) xref.push("<a href='http://www.cancer.gov/drugdictionary?CdrID="+nci+"'>NCI</a>");
//                                var pharmgkb = drug[7]['PharmGKB'];
//                                if (pharmgkb) xref.push("<a href='http://www.pharmgkb.org/views/index.jsp?objId="+pharmgkb+"'>PharmGKB</a>");
//                                var drugbank = drug[7]['DrugBank'];
//                                if (drugbank) xref.push("<a href='http://www.drugbank.ca/drugs/"+drugbank+"'>DrugBank</a>");
//                                var keggdrug = drug[7]['KEGG Drug'];
//                                if (keggdrug) xref.push("<a href='http://www.genome.jp/dbget-bin/www_bget?dr:"+keggdrug+"'>KEGG Drug</a>");
//                                
//                                if (xref.length) {
//                                    txtDrug.push("Data sources:</b></td><td>"+xref.join(",&nbsp;"));
//                                }
//                            }
                            if (drug[8]>0) {
                                var nci = drug[7]['NCI_Drug'];
                                if (nci) {
                                    txtDrug.push("Clinical Trials:</b></td><td><a href='http://www.cancer.gov/Search/ClinicalTrialsLink.aspx?idtype=1&id="+nci+"'>"+drug[8]+" clinical trial"+(drug[8]>1?"s":"")+"</a>");
                                }
                            }
                            txt.push("<table><tr valign='top'><td nowrap='nowrap'><b>"+txtDrug.join("</td></tr><tr valign='top'><td nowrap='nowrap'><b>")+"</td></tr></table>");
                        }
                        var html = txt.join('<hr><br/>');
                        this.set('content.text', html);
                    }
                }
            },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded qtip-wide' },
            position: { my: my, at: at,viewport: $(window) }
        });
    });
}

/**
* modified from http://jsfiddle.net/H2SKt/1/
**/
function d3PieChart(svg, data, radius, colors) {
    var chart = svg
        .data([data])
        .append("g")
        .attr("transform", "translate(" + radius + "," + radius + ")");

    var arc = d3.svg.arc()
        .outerRadius(radius);

    var pie = d3.layout.pie()
        .value(function(d) { return d; })
        .sort(null);

    var arcs = chart.selectAll("g.slice")
        .data(pie) 
        .enter()
        .append("g")
        .attr("class", "slice");

    arcs.append("path")
        .attr("fill", function(d, i) { return colors[i]; } )
        .attr("d", arc);

    return chart;
}

function d3AccBar(svg, data, width, colors) {
    var acc = [];
    var sum = 0;
    for (var i=0; i<data.length; i++) {
        acc.push(sum);
        sum += data[i];
    }
    
    var vd = [];
    for (var i=0; i<data.length; i++) {
        vd.push({
            start: width*acc[i]/sum,
            width: width*data[i]/sum,
            color: colors[i]
        });
    }

    var chart = svg.selectAll(".bar")
        .data(vd) 
        .enter()
        .append("g")
        .attr("class", "bar")
        .attr("transform", function(d,i) { return "translate(" + d.start + "," + 3 + ")"; });

    chart.append("rect")
        .attr("width", function(d, i) { return d.width; })
        .attr("height", 8)
        .attr("fill", function(d, i) { return d.color; } );

    return chart;
}

function d3CircledChar(g,ch,circleColor,textColor) {
    g.append("circle")
        .attr("r",5)
        .attr("stroke",circleColor)
        .attr("fill","none");
    g.append("text")
        .attr("x",-3)
        .attr("y",3)
        .attr("font-size",7)
        .attr("fill",textColor)
        .text(ch);
}
    
function plotMrna(div,alts) {
    $(div).each(function() {
        if (!$(this).is(":empty")) return;
        var gene = $(this).attr("alt");
        var mrna = alts.getValue(gene, 'mrna');
        d3MrnaBar($(this)[0],mrna.perc);
        $(this).qtip({
            content: {text: "mRNA level of the gene in this tumor<br/><b>mRNA z-score</b>: "
                        +mrna.zscore.toFixed(2)+"<br/><b>Percentile</b>: "+mrna.perc+"%"},
	        show: {event: "mouseover"},
            hide: {fixed: true, delay: 10, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded' },
            position: {my:'top left',at:'bottom center',viewport: $(window)}
        });
    });
}

function d3MrnaBar(div,mrnaPerc) {
    var textWidth = 30,
        graphWidth = 30,
        circleR = 3,
        width = graphWidth+textWidth+2*circleR,
        height = 12;

    var svg = d3.select(div).append('svg')
        .attr("width", width)
        .attr("height", height);

    svg.append("text")
        .attr("x", width)
        .attr('y',11)
        .attr("text-anchor", "end")
        .attr('font-size',10)
        .text(mrnaPerc+"%");

    var bar = svg.append("g")
                .attr("transform", "translate(" + circleR + "," + 0 + ")");

    bar.append("line")
        .attr("x1",-circleR)
        .attr("y1",height/2)
        .attr("x2",graphWidth+circleR)
        .attr("y2",height/2)
        .attr("style", "stroke:gray;stroke-width:2");

    bar.append("circle")
        .attr("cx", graphWidth * mrnaPerc/100)
        .attr("cy", height/2)
        .attr("r", circleR)
        .attr("fill", mrnaPerc>75 ? "red" : (mrnaPerc<25?"blue":"gray"));

}

function plotAlleleFreq(div,mutations,altReadCount,refReadCount) {
    $(div).each(function() {
        if (!$(this).is(":empty")) return;
        var gene = $(this).attr("alt");
        var refCount = mutations.getValue(gene, refReadCount);
        var altCount = mutations.getValue(gene, altReadCount);
        var allFreq = {};
        for (var caseId in refCount) {
            var ac = altCount[caseId];
            var rc = refCount[caseId];
            if (ac&&rc) allFreq[caseId] = (ac/(ac+rc)).toFixed(2);
        }
        d3AlleleFreqBar($(this)[0],allFreq);
        
        // tooltip
        var arr = [];
        caseIds.forEach(function(caseId){
            var ac = altCount[caseId];
            var rc = refCount[caseId];
            if (ac&&rc) arr.push("<svg width='12' height='12' class='case-label-tip' alt='"+caseId+"'></svg>&nbsp;<b>"
                    +(ac/(ac+rc)).toFixed(2)+"</b>&nbsp;("+ac+" variant reads out of "+(ac+rc)+" total)");
        });
        var tip = arr.join("<br/>");
        $(this).qtip({
            content: {text: tip},
            events: {
                render: function(event, api) {
                    plotCaseLabel('.case-label-tip', true, true);
                }
            },
	        show: {event: "mouseover"},
            hide: {fixed: true, delay: 10, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded' },
            position: {my:'top left',at:'bottom center',viewport: $(window)}
        });
    });
}

function d3AlleleFreqBar(div,alleFreq) {
    var barWidth = 6,
        barMargin = 3,
        width = (barWidth+barMargin)*caseIds.length,
        height = 12;

    var y = d3.scale.linear()
        .domain([0, 1])
        .range([0, height]);

    var svg = d3.select(div).append('svg')
        .attr("width", width)
        .attr("height", height);

    var chart = svg.selectAll(".bar")
        .data(caseIds) 
        .enter()
        .append("g")
        .attr("class", "bar")
        .attr("transform", function(caseId,i) { return "translate(" + ((barWidth+barMargin)*i)
            + "," + y(1-(alleFreq[caseId]?alleFreq[caseId]:0)) + ")"; });

    chart.append("rect")
        .attr("width", barWidth)
        .attr("height", function(caseId,i) { return y(alleFreq[caseId]?alleFreq[caseId]:0);})
        .attr("fill", function(caseId, i) { return caseMetaData.color[caseId]; } );

}

function trimHtml(html) {
    return html.replace(/<[^>]*>/g,"");
}

function idRegEx(ids) {
    return "(^"+ids.join("$)|(^")+"$)";
}

function guessClinicalData(clinicalData, paramNames) {
    if (!clinicalData) return null;
    for (var i=0, len=paramNames.length; i<len; i++) {
        var data = clinicalData[paramNames[i]];
        if (typeof data !== 'undefined' && data !== null) return data;
    }
    return null;
}

function outputClinicalData() {
    $("#clinical_div").append("<table id='clinical_table' width='100%'></table>");
    var n=caseIds.length;
    if (n>1) initCaseMetaData();
    
    // first row -- cancer study and nav
    $("#clinical_table").append("<tr><td>"+formatCancerStudyInfo()+"</td><td>"+formatNav()+"</td></tr>");
    initNav();
    
    // for each sample
    for (var i=0; i<n; i++) {
        var caseId = caseIds[i];
        var clinicalData = clinicalDataMap[caseId];
        
        var row = "<tr><td><b><u><a href='"+cbio.util.getLinkToSampleView(cancerStudyId,caseId)+"'>"+caseId+"<a></b></u>&nbsp;";
        if (n>1) {
            row += "<svg width='12' height='12' class='case-label-header' alt='"+caseId+"'></svg>&nbsp;";
        }
        
        var info = [];
        var info = info.concat(formatPatientInfo(clinicalData));
        var info = info.concat(formatDiseaseInfo(clinicalData));
        var info = info.concat(formatPatientStatus(clinicalData));
        row +=info.join(",&nbsp;");
       
        row += "</td><td align='right'><a href='#' class='more-clinical-a' alt='"+caseId+"'>More about this tumor</a></td></tr>";
        $("#clinical_table").append(row);
        
    }
    addMoreClinicalTooltip(".more-clinical-a");
    
    if (n>1) {
        plotCaseLabel('.case-label-header', false, true);
    }
  
    function initCaseMetaData() {
        var n=caseIds.length;
        // set caseMetaData.color
        for (var i=0; i<n; i++) {
            var caseId = caseIds[i];
            var clinicalData = clinicalDataMap[caseId];
            var state = guessClinicalData(clinicalData, ["TUMOR_TYPE","SAMPLE_TYPE"]);
            caseMetaData.color[caseId] = getCaseColor(state);
        }

        // reorder based on color
//        var colors = {black:1, orange:2, red:3};
//        caseIds.sort(function(c1, c2){
//            var ret = colors[caseMetaData.color[c1]]-colors[caseMetaData.color[c2]];
//            if (ret===0) return c1<c2?-1:1;
//            return ret;
//        });
        caseMetaData.index = cbio.util.arrayToAssociatedArrayIndices(caseIds);

        // alt 1: set labels by color group
        /*
        var mapColorCases = {};
        caseIds.forEach(function (caseId) {
            var color = caseMetaData.color[caseId];
            if (!(color in mapColorCases)) mapColorCases[color] = [];
            mapColorCases[color].push(caseId);
        });
        for (var color in mapColorCases) {
            var cases = mapColorCases[color];
            var len = cases.length;
            if (len===1) {
                caseMetaData.label[cases[0]]='';
            } else {
                for (var i=0; i<len; i++){
                    var _case = cases[i];
                    caseMetaData.label[_case] = i+1;
                };
            }
        }*/
        // alt 2: set labels all together
        for (var i=0; i<caseIds.length; i++) {
            caseMetaData.label[caseIds[i]] = i+1;
        }
        

        // set tooltips
        for (var i=0; i<n; i++) {
            var caseId = caseIds[i];
            var clinicalData = clinicalDataMap[caseId];

            var tip = "<tr><td><b><u>"+"<a href='"+cbio.util.getLinkToSampleView(cancerStudyId,caseId)+"'>"+caseId+"</a>"+"</b></u>";

            var stateInfo = formatStateInfo(clinicalData);
            if (stateInfo) tip +="&nbsp;"+stateInfo;

            caseMetaData.tooltip[caseId] = tip;
        }
    }
    
    function formatPatientInfo(clinicalData) {
        var patientInfo = [];
        var gender = guessClinicalData(clinicalData, ['GENDER']);
        if (gender!==null)
            patientInfo.push(gender);
        var age = guessClinicalData(clinicalData, ['AGE']);
        if (age!==null)
            patientInfo.push(Math.floor(age) + " years old");

        return patientInfo;
    }
    
    function formatStateInfo(clinicalData) {
        var ret = null;
        var caseType = guessClinicalData(clinicalData, ["TUMOR_TYPE","SAMPLE_TYPE"]);
        if (caseType!==null) {
            ret = "<font color='"+getCaseColor(caseType)+"'>"+caseType+"</font>";
            var loc;
            if (normalizedCaseType(caseType.toLowerCase()) === "metastasis") {
                loc = guessClinicalData(clinicalData,["TUMOR_SITE","METASTATIC_SITE"]);
            } else {
                loc = guessClinicalData(clinicalData,["TUMOR_SITE","PRIMARY_SITE"]);
            }
            if (loc!==null) 
                ret += " ("+loc+")";
        }
        return ret;
    }
    
    function formatCancerStudyInfo() {
        return "<a href=\"study.do?cancer_study_id="+cancerStudyId+"\"><b>"+cancerStudyName+"</b></a>";
    }

    function formatNav() {
        if (!CaseNavigation.hasNavCaseIds()) return "";
        return "<ul class='pager' style='float:right;'>Viewing #"+(CaseNavigation.currPosition()+1)+" of "+CaseNavigation.numOfNavCases()+" cases&nbsp&nbsp;"
                    + "<li id='case-navigate-first'><a "+(CaseNavigation.hasPrevious()?("href='"+CaseNavigation.first()+"'"):"")+">&lt;&lt;</a></li>&nbsp;"
                    + "<li id='case-navigate-previous'><a "+(CaseNavigation.hasPrevious()?("href='"+CaseNavigation.previous()+"'"):"")+">&nbsp;&lt;&nbsp;</a></li>&nbsp;"
                    + "<li id='case-navigate-next'><a "+(CaseNavigation.hasNext()?("href='"+CaseNavigation.next()+"'"):"")+">&nbsp;&gt;&nbsp;</a></li>&nbsp;"
                    + "<li id='case-navigate-last'><a "+(CaseNavigation.hasNext()?("href='"+CaseNavigation.last()+"'"):"")+">&gt;&gt;</a></li></ul>";
    }
    
    function initNav() {
        if (!CaseNavigation.hasNavCaseIds()) return;
        if (!CaseNavigation.hasPrevious()) {
            $("#case-navigate-first").addClass("disabled");
            $("#case-navigate-previous").addClass("disabled");
        } else {
            //$("#case-navigate-first").click(CaseNavigation.navToFirst);
            $("#case-navigate-previous").click(CaseNavigation.navToPrevious);
        }
        if (!CaseNavigation.hasNext()) {
            $("#case-navigate-last").addClass("disabled");
            $("#case-navigate-next").addClass("disabled");
        } else {
            $("#case-navigate-last").click(CaseNavigation.navToLast);
            $("#case-navigate-next").click(CaseNavigation.navToNext);
        }
    }

    function formatDiseaseInfo(clinicalData) {
        var diseaseInfo = [];
        
        var typeOfCancer = guessClinicalData(clinicalData,["TYPE_OF_CANCER", "CANCER_TYPE"]);
        if (typeOfCancer!==null) {
            diseaseInfo.push(typeOfCancer);
        }
        
        var detailedCancerType = guessClinicalData(clinicalData,["DETAILED_CANCER_TYPE","CANCER_TYPE_DETAILED"]);
        if (detailedCancerType!==null) {
            diseaseInfo.push(detailedCancerType);
        } 
        
        var knowMolecularClassifier = guessClinicalData(clinicalData,["KNOWN_MOLECULAR_CLASSIFIER"]);
        if (knowMolecularClassifier!==null) {
            diseaseInfo.push(knowMolecularClassifier);
        } 

        var stateInfo = formatStateInfo(clinicalData);
        if (stateInfo) diseaseInfo.push(stateInfo);

        var gleason = guessClinicalData(clinicalData,
                        ["GLEASON_SCORE"]);
        var strGleason = null;
        if (gleason!==null) {
            strGleason = "Gleason: "+gleason;
        } 

        var primaryGleason = guessClinicalData(clinicalData, ["GLEASON_SCORE_1"]);
        var secondaryGleason = guessClinicalData(clinicalData, ["GLEASON_SOCRE_2"]);
        if (primaryGleason!==null && secondaryGleason!==null) {
            strGleason += " (" + primaryGleason + "+" + secondaryGleason + ")";
        }
        if (gleason) diseaseInfo.push(strGleason);

        var histology = guessClinicalData(clinicalData,["HISTOLOGY"]);
        if (histology!==null) {
            diseaseInfo.push(histology);
        }

        var stage = guessClinicalData(clinicalData, ["TUMOR_STAGE_2009"]);
        if (stage!==null && stage.toLowerCase()!=="unknown") {
            diseaseInfo.push(stage); 
        }

        var grade = guessClinicalData(clinicalData,["TUMOR_GRADE"]);
        if (grade!==null) {
            diseaseInfo.push(grade);
        }

        // TODO: this is a hacky way to include the information in prad_mich
        var etsRafSpink1Status = guessClinicalData(clinicalData,["ETS/RAF/SPINK1_STATUS"]);
        if (etsRafSpink1Status!==null) {
            diseaseInfo.push(etsRafSpink1Status);
        }

        // TODO: this is a hacky way to include the information in prad_broad
        var tmprss2ErgFusionStatus = guessClinicalData(clinicalData,["TMPRSS2-ERG_FUSION_STATUS"]);
        if (tmprss2ErgFusionStatus!==null) {
            diseaseInfo.push("TMPRSS2-ERG Fusion: "+tmprss2ErgFusionStatus);
        }

        // TODO: this is a hacky way to include the information in prad_mskcc
        var ergFusion = guessClinicalData(clinicalData, ["ERG-FUSION_ACGH"]);
        if (ergFusion!==null) {
            diseaseInfo.push("ERG-fusion aCGH: "+ergFusion);
        }

        // TODO: this is a hacky way to include the serum psa information for prad
        var serumPsa = guessClinicalData(clinicalData, ["SERUM_PSA"]);
        if (serumPsa!==null) {
            diseaseInfo.push("Serum PSA: "+serumPsa);
        }

        return diseaseInfo;
    }

    function formatPatientStatus(clinicalData) {
        var oss = guessClinicalData(clinicalData, ["OS_STATUS"]);
        var ossLow = oss===null?null:oss.toLowerCase();
        var dfss = guessClinicalData(clinicalData, ["DFS_STATUS"]);
        var dfssLow = dfss===null?null:dfss.toLowerCase();
        var osm = guessClinicalData(clinicalData, ["OS_MONTHS"]);
        var dfsm = guessClinicalData(clinicalData, ["DFS_MONTHS"]);
        var ret = [];
        if (oss!==null && ossLow!=="unknown") {
            var patientStatus = "<font color='"
                    + (ossLow==="living"||ossLow==="alive" ? "green":"red")
                    + "'>"
                    + oss
                    + "</font>";
            if (osm!==null && osm!=='NA') {
                patientStatus += " (" + Math.round(osm) + " months)";
            }
            ret.push(patientStatus);
        }
        if (dfss!==null && dfssLow!=="unknown") {            
            var patientStatus = "<font color='"
                    + (dfssLow==="diseasefree" ? "green":"red")
                    + "'>"
                    + dfss
                    + "</font>";
            if (dfsm!==null && dfsm!=='NA') {
                patientStatus += " (" + Math.round(dfsm) + " months)";
            }
            ret.push(patientStatus);
        }
        return ret;
    }

    function getCaseColor(caseType) {
        if (!caseType) return "black";
        var caseTypeNorm = normalizedCaseType(caseType.toLowerCase());
        if (caseTypeNorm==="primary") return "black";
        if (caseTypeNorm==="metastasis") return "red";
        if (caseTypeNorm==="progressed") return "orange";
        return "black";
    }
    
    function normalizedCaseType(caseType) {
        var caseTypeLower = caseType.toLowerCase();
        if (caseTypeLower.indexOf("metastatic")>=0 || caseTypeLower.indexOf("metastasis")>=0)
            return "metastasis";
        if (caseTypeLower.indexOf("progressed")>=0
                || caseTypeLower.indexOf("progression")>=0
                || caseTypeLower.indexOf("recurred")>=0
                || caseTypeLower.indexOf("recurrence")>=0)
            return "progressed";
        
        return "primary";
    }
}

function plotCaseLabel(svgEl,onlyIfEmpty, noTip) {
    $(svgEl).each(function() {
        if (onlyIfEmpty && !$(this).is(":empty")) return;
        var caseId = $(this).attr('alt');
        
        var svg = d3.select($(this)[0]);
    
        if (caseId) {
            plotCaselabelInSVG(svg, caseId);
            if (!noTip) addNoteTooltip($(this), caseMetaData.tooltip[caseId], {my:'middle left',at:'middle right',viewport: $(window)});
        }
    });
}

function plotCaselabelInSVG(svg, caseId) {
    if(!svg) return;
    var circle = svg.append("g")
        .attr("transform", "translate(6,6)");
    circle.append("circle")
        .attr("r",6);
    fillColorAndLabelForCase(circle, caseId);
}

function fillColorAndLabelForCase(circle, caseId) {
    var label = caseMetaData.label[caseId];
    var color = caseMetaData.color[caseId];
    circle.select("circle").attr("fill",color);
    circle.append("text")
        .attr("x",-3)
        .attr("y",4)
        .attr("font-size",10)
        .attr("fill","white")
        .text(label);
}

var CaseNavigation = (function(currCaseId){
    var navCaseIds = (function(){
        var idStr = /nav_case_ids=(.+)/.exec(location.hash);
        if (!idStr) return [];
        return idStr[1].split(/[ ,]+/);
    })();
    
    var currPosition = (function(){
        return $.inArray(currCaseId, navCaseIds);
    })();
    
    function hasNavCaseIds() {
        return navCaseIds.length>0;
    }
    
    function hasPrevious() {
        return hasNavCaseIds() && currPosition>0;
    }
    
    function previousCaseId() {
        return hasPrevious() ? navCaseIds[currPosition-1] : null;
    }
    
    function getUrlTo(id) {
        return window.location.href.replace("="+currCaseId, "="+id);
    }
    
    function navTo(id) {
        window.location.replace(getUrlTo(id));
    }
    
    function hasNext() {
        return hasNavCaseIds() && (currPosition<navCaseIds.length-1);
    }
    
    function nextCaseId() {
        return hasNext ? navCaseIds[currPosition+1] : null;
    }
    
    return {
        currPosition: function() {return currPosition;},
        numOfNavCases: function() {return navCaseIds.length;},
        hasNavCaseIds : hasNavCaseIds,
        hasPrevious : hasPrevious,
        hasNext : hasNext,
        first: function() {
            return getUrlTo(navCaseIds[0]);
        },
        previous: function() {
            return getUrlTo(previousCaseId());
        },
        next: function() {
            return getUrlTo(nextCaseId());
        },
        last: function() {
            return getUrlTo(navCaseIds[navCaseIds.length-1]);
        },
        navToFirst: function() {
                navTo(navCaseIds[0]);
            },
        navToPrevious : function() {
                navTo(previousCaseId());
            },
        navToNext : function() {
                navTo(nextCaseId());
            },
        navToLast: function() {
                navTo(navCaseIds[navCaseIds.length-1]);
            }
    };
})(isPatientView?patientId:caseIds[0]);

window["<%=PatientView.CANCER_STUDY_META_DATA_KEY_STRING%>"]
        = <%=jsonMapper.writeValueAsString(request.getAttribute(PatientView.CANCER_STUDY_META_DATA_KEY_STRING))%>;

</script>

</body>
</html>
