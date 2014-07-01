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
 *
 * - Generate the plots tab "global" data object (Being used in every sub tabs)
 * - AJAX data retrieving function (using JSON servlet)
 * - Cache every generated data set in a global variable
 *
 */

var PlotsTabView = (function(){

    var log_scale_threshold = 0.17677669529;  // 2 to the -2.5

    function addxAxisHelp(svg, axisGroupSvg, xTitle, xTitleClass, xText) {
        axisGroupSvg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", xTitleClass)
            .attr("x", 350 + xTitle.length / 2 * 8)
            .attr("y", 567)
            .attr("width", "16")
            .attr("height", "16");
        svg.select("." + xTitleClass).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + xText + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            }
        );
    }

    function addyAxisHelp(svg, axisGroupSvg, yTitle, yTitleClass, yText) {
        axisGroupSvg.append("svg:image")
            .attr("xlink:href", "images/help.png")
            .attr("class", yTitleClass)
            .attr("x", 34)
            .attr("y", 255 - yTitle.length / 2 * 8)
            .attr("width", "16")
            .attr("height", "16");
        svg.select("." + yTitleClass).each(
            function() {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + yText + "</font>"},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'right bottom',at:'top left', viewport: $(window)}
                    }
                );
            }
        );
    }

    function searchPlots(viewIdentifier) {
        var searchToken = "";
        if (viewIdentifier === "one_gene") {
            searchToken = document.getElementById("search_plots_one_gene").value;
        } else if (viewIdentifier === "two_genes") {
            searchToken = document.getElementById("search_plots_two_genes").value;
        } else if (viewIdentifier === "custom") {
            searchToken = document.getElementById("search_plots_custom").value;
        }
        d3.select("#plots_box").selectAll("path").each(
            function() {
                var _attr = $(this).attr("class");
                if (typeof _attr !== 'undefined' && _attr !== false && _attr !== "domain") {
                    if ( searchToken.length >= 4 ) {
                        if ( $(this).attr("class").toUpperCase().indexOf(searchToken.toUpperCase()) !== -1 &&
                            (searchToken.toUpperCase()) !== "TCGA" && (searchToken.toUpperCase()) !== "TCGA-") {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size") + 5)
                                .type(d3.select(this).attr("symbol")));
                        } else {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size"))
                                .type(d3.select(this).attr("symbol")));
                        }
                    } else {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size"))
                            .type(d3.select(this).attr("symbol")));
                    }
                }
            }
        );
    }

    return {
        viewInitCallback: function() { //Register all the sub view init funcs here!!
            PlotsTabMenu.init();
            OneGene.init();
        },
        init: function() {
        	PlotsTabMenuDataProxy.init();
        },
        getGeneticProfiles: function(selectedGene) {
            return genetic_profiles[selectedGene];
        },
        getClinicalAttributes: function() {
            return clinical_attributes;
        },
        getProfileData: function(gene, genetic_profile_id, case_set_id, case_ids_key, callback_func) {
            var paramsGetProfileData = {
                cancer_study_id: cancer_study_id,
                gene_list: gene,
                genetic_profile_id: genetic_profile_id,
                case_set_id: case_set_id,
                case_ids_key: case_ids_key
            };
            $.post("getProfileData.json", paramsGetProfileData, callback_func, "json");
        },
        getMutationType: function(gene, genetic_profile_id, case_set_id, case_ids_key, callback_func) {
            var proxy = DataProxyFactory.getDefaultMutationDataProxy();
            proxy.getMutationData(gene, callback_func);
        },
        addxAxisHelp: addxAxisHelp,
        addyAxisHelp: addyAxisHelp,
        searchPlots: searchPlots,
        getLogScaleThreshold: function() {
            return log_scale_threshold;
        }
    };

}());    //Closing Plots

// Takes the content in the plots svg element
// and returns XML serialized *string*
function loadPlotsSVG() {
    //Remove the help icons
    var elemXHelpTxt = $(".x-title-help").qtip('api').options.content.text;
    var elemYHelpTxt = $(".y-title-help").qtip('api').options.content.text;
    var elemXHelp = $(".x-title-help").remove();
    var elemYHelp = $(".y-title-help").remove();
    //Extract SVG
    var result = $("#plots_box").html();
    //Add the help icons back on
    $(".axis").append(elemXHelp);
    $(".axis").append(elemYHelp);
    $(".x-title-help").qtip({
        content: {text: elemXHelpTxt },
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'left bottom',at:'top right'}
    });
    $(".y-title-help").qtip({
        content: {text: elemYHelpTxt },
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'right bottom',at:'top left', viewport: $(window)}
    });

    return result;
}





