/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

/**
 * Render the Co-expression view using dataTable Jquery Plugin
 *
 * User: yichao
 * Date: 12/5/13
 */

var CoExpTable = (function() {

    var Names = {
        divPrefix: "coexp_",
        tablePrefix: "coexp_table_",
        loadingImgPrefix: "coexp_loading_img_"
    };

    var CoExpTable = (function() {

        function getCoExpData(geneId) {
            var paramsGetCoExpData = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene: geneId,
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            };
            $.post("getCoExp.do", paramsGetCoExpData, getCoExpDataCallBack(geneId), "json");
        }

        function getCoExpDataCallBack(geneId) {
            return function(result) {
                //figure out div id
                var divId = Names.divPrefix + geneId;
                var tableId = Names.tablePrefix + geneId;
                var loadingImgId = Names.loadingImgPrefix + geneId;

                //Render
                $("#" + loadingImgId).hide();

                $("#" + divId).append(
                    "<table id='" + tableId + "' cellpadding='0' cellspacing='0' border='0' class='display'></table>"
                );
                $("#" + tableId).append(
                    "<thead style='font-size:70%;' >" +
                    "<tr><th>Compared Gene</th><th>Pearson's Score</th><th>Plots</th></tr>" +
                    "</thead><tbody></tbody>"
                );
                $("#" + tableId).dataTable({
                    "sDom": '<"H"if>t<"F"lp>',
                    "sPaginationType": "full_numbers",
                    "bJQueryUI": true,
                    "bAutoWidth": false
                });
                attachDataToTable(result, tableId);
            }

        }

        function attachDataToTable(result, tableId) {
            $.each(result, function(i, _obj) {
                $("#" + tableId).dataTable().fnAddData([_obj.gene, _obj.pearson, "(+)"]);
            });
        }

        return {
            init: function(geneId) {
                var element =  document.getElementById(Names.tablePrefix + geneId);
                if (typeof(element) === 'undefined' || element === null) {
                    getCoExpData(geneId);
                }
            }
        }
    }());

    var Tabs = (function() {

        function appendTabsContent() {
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-list").append("<li><a href='#" + Names.divPrefix + value + "' class='coexp-tabs-ref'><span>" + value + "</span></a></li>");
            });
            $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                $("#coexp-tabs-content").append("<div id='" + Names.divPrefix + value + "'>" +
                    "<div id='" + Names.loadingImgPrefix + value + "'>" +
                    "<img style='padding:200px;' src='images/ajax-loader.gif'>" +
                    "</div></div>");
            });
        }

        function generateTabs() {
            $("#coexp-tabs").tabs();
            $("#coexp-tabs").tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
            $("#coexp-tabs").tabs('select', 0);
        }

        function bindListenerToTabs() {
            $("#coexp-tabs").bind('tabsselect', function(event, ui) {
                var _genes = window.PortalGlobals.getGeneList();
                var _gene = _genes[ui.index];
                CoExpTable.init(_gene);

            });
        }


        return {
            appendTabsContent: appendTabsContent,
            generateTabs: generateTabs,
            bindListenerToTabs: bindListenerToTabs
        }
    }());

    return {
        initTabs: function() {
            Tabs.appendTabsContent();
            Tabs.generateTabs();
            Tabs.bindListenerToTabs();
        },
        initView: function() {
            var _genes = window.PortalGlobals.getGeneList();
            //CoExpTable.init(_genes[0]);
            console.log(_genes[0]);
        }
    };

}());    //Closing CoExpTable