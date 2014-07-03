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


var OneGene = (function () {

    var text = {
            mutations_alias : {
                frameshift : "frameshift",
                in_frame : "in_frame",
                missense : "missense",
                nonsense : "nonsense",
                splice : "splice",
                nonstop : "nonstop",
                nonstart : "nonstart",
                non : "non"
            },
            gistic_txt_val : {
                "-2": "Homdel",
                "-1": "Hetloss",
                "0": "Diploid",
                "1": "Gain",
                "2": "Amp"
            }
        },   //Text for the general view
        mutationStyle = {  //Key and "typeName" are always identical
            frameshift : {
                typeName : "frameshift",
                symbol : "triangle-down",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Frameshift"
            },
            nonsense : {
                typeName: "nonsense",
                symbol : "diamond",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonsense"
            },
            splice : {
                typeName : "splice",
                symbol : "triangle-up",
                fill : "#A4A4A4",
                stroke : "#B40404",
                legendText : "Splice"
            },
            in_frame : {
                typeName : "in_frame",
                symbol : "square",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "In_frame"
            },
            nonstart : {
                typeName : "nonstart",
                symbol : "cross",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Nonstart"
            },
            nonstop : {
                typeName : "nonstop",
                symbol : "triangle-up",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Nonstop"
            },
            missense : {
                typeName : "missense",
                symbol : "circle",
                fill : "#DF7401",
                stroke : "#B40404",
                legendText : "Missense"
            },
            other: {
                typeName: "other",
                symbol: "square",
                fill : "#1C1C1C",
                stroke : "#B40404",
                legendText : "Other"
            },
            non : {
                typeName : "non",
                symbol : "circle",
                fill : "#00AAF8",
                stroke : "#0089C6",
                legendText : "No mutation"
            }
        },
        gisticStyle = {
            Amp : {
                stroke : "#FF0000",
                fill : "none",
                symbol : "circle",
                legendText : "Amp"
            },
            Gain : {
                stroke : "#FF69B4",
                fill : "none",
                symbol : "circle",
                legendText : "Gain"
            },
            Diploid : {
                stroke : "#000000",
                fill : "none",
                symbol : "circle",
                legendText : "Diploid"
            },
            Hetloss : {
                stroke : "#00BFFF",
                fill : "none",
                symbol : "circle",
                legendText : "Hetloss"
            },
            Homdel : {
                stroke : "#00008B",
                fill : "none",
                symbol : "circle",
                legendText : "Homdel"
            }
        },
        userSelection = {
            gene: "",
            plots_type: "",
            copy_no_type : "",
            mrna_type : "",
            dna_methylation_type : "",
            rppa_type : ""
        };   //current user selection from the side menu

    var discretizedDataTypeIndicator = "";

    var View = (function() {

        var elem = {
                svg : "",
                elemDotsGroup : "",
                boxPlots: ""
            },   //DOM elements
            settings = {
                canvas_width: 750,
                canvas_height: 600
            },   //basic d3 canvas settings
            attr = {
                xScale : "",
                yScale : ""
            };

        var Axis = (function() {

            var xAxis = "",
                yAxis = "",
                xTitle = "",
                yTitle = "",
                xTitleHelp = "",
                yTitleHelp = "";

            function getAxisTitles() {
                //TODO: Change hard-coded menu items value
                if (OneGeneUtil.plotsTypeIsCopyNo()) {
                    var e = document.getElementById("data_type_copy_no");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (OneGeneUtil.plotsTypeIsMethylation()) {
                    var e = document.getElementById("data_type_dna_methylation");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_mrna");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                } else if (OneGeneUtil.plotsTypeIsRPPA()) {
                    var e = document.getElementById("data_type_mrna");
                    xTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    xTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                    e = document.getElementById("data_type_rppa");
                    yTitle = userSelection.gene + ", " + e.options[e.selectedIndex].text;
                    yTitleHelp = e.options[e.selectedIndex].value.split("|")[1];
                }
            }

            function initDiscretizedAxis() {
                var _dataAttr = OneGeneDataProxy.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                //reset max_x as the range of slots
                // -- Not real max x value for scaling!!
                var slotsCnt = 0;
                var tmp_copy_no = [];
                $.each(OneGeneDataProxy.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                })
                for (var j = -2; j < 3; j++) {
                    if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                        slotsCnt += 1;
                    }
                }
                //Set the domain range for different cases
                var new_min_x, new_max_x;
                if (slotsCnt === 1) {
                    new_min_x = -0.5;
                    new_max_x = 0.5;
                } else if (slotsCnt === 2) {
                    new_min_x = -0.8;
                    new_max_x = 1.8;
                } else if (slotsCnt === 3) {
                    new_min_x = -0.8;
                    new_max_x = 2.8;
                } else if (slotsCnt === 4) {
                    new_min_x = -0.6;
                    new_max_x = 3.6;
                } else if (slotsCnt === 5) {
                    new_min_x = -0.6;
                    new_max_x = 4.6;
                }
                var edge_y = (max_y - min_y) * 0.1;
                //Define the axis
                attr.xScale = d3.scale.linear()
                    .domain([new_min_x, new_max_x])
                    .range([100, 600]);
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawDiscretizedAxis() {
                var textSet = [];
                var svg = elem.svg;
                var tmp_copy_no = [];
                $.each(OneGeneDataProxy.getDotsGroup(), function(index, value) {
                    tmp_copy_no.push(value.xVal);
                })
                for (var j = -2; j < 3; j++) {
                    if (tmp_copy_no.indexOf(j.toString()) !== -1) {
                        textSet.push(text.gistic_txt_val[j.toString()]);
                    }
                }
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis.ticks(textSet.length))
                    .selectAll("text")
                    .data(textSet)
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .text(function(d){return d});
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
                svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function initContinuousAxisX() {
                var _dataAttr = OneGeneDataProxy.getDataAttr();
                var min_x = _dataAttr.min_x;
                var max_x = _dataAttr.max_x;
                var edge_x = (max_x - min_x) * 0.2;
                if (OneGeneUtil.plotsTypeIsMethylation()){
                    //Range for DNA Methylation Data Type
                    //Need to be fixed as from 0 to 1.
                    attr.xScale = d3. scale.linear()
                        .domain([-0.02, 1.02])
                        .range([100,600]);
                } else {
                    attr.xScale = d3.scale.linear()
                        .domain([min_x - edge_x, max_x + edge_x])
                        .range([100, 600]);
                }
                xAxis = d3.svg.axis()
                    .scale(attr.xScale)
                    .orient("bottom")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function initContinuousAxisY() {
                var _dataAttr = OneGeneDataProxy.getDataAttr();
                var min_y = _dataAttr.min_y;
                var max_y = _dataAttr.max_y;
                var edge_y = (max_y - min_y) * 0.1;
                attr.yScale = d3.scale.linear()
                    .domain([min_y - edge_y, max_y + edge_y])
                    .range([520, 20]);
                yAxis = d3.svg.axis()
                    .scale(attr.yScale)
                    .orient("left")
                    .tickSize(6, 0, 0)
                    .tickPadding([8]);
            }

            function drawContinuousAxisMainX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 520)")
                    .attr("class", "plots-x-axis-class")
                    .call(xAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawContinuousAxisEdgeX() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(xAxis.orient("bottom").ticks(0));
            }

            function drawContinuousAxisMainY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(100, 0)")
                    .attr("class", "plots-y-axis-class")
                    .call(yAxis)
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black");
            }

            function drawContinuousAxisEdgeY() {
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(600, 0)")
                    .call(yAxis.orient("left").ticks(0));
            }

            function addXaxisTitle(axisTitleGroup, xTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "x-axis-title")
                    .attr("x", 350)
                    .attr("y", 580)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(xTitle);

            }

            function addYaxisTitle(axisTitleGroup, yTitle) {
                axisTitleGroup.append("text")
                    .attr("class", "y-axis-title")
                    .attr("transform", "rotate(-90)")
                    .attr("x", -270)
                    .attr("y", 45)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text(yTitle);

            }

            function addxAxisHelp(axisTitleGroup, _xTitle) {
                Plots.addxAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _xTitle,
                    "x-title-help",
                    xTitleHelp
                );
            }

            function addyAxisHelp(axisTitleGroup, _yTitle) {
                Plots.addyAxisHelp(
                    elem.svg,
                    axisTitleGroup,
                    _yTitle,
                    "y-title-help",
                    yTitleHelp
                );

            }

            return {
                init: function() {
                    getAxisTitles();
                    if (OneGeneUtil.plotsIsDiscretized()) {
                        initDiscretizedAxis();
                        drawDiscretizedAxis();
                    } else {
                        initContinuousAxisX();
                        initContinuousAxisY();
                        drawContinuousAxisMainX();
                        drawContinuousAxisEdgeX();
                        drawContinuousAxisMainY();
                        drawContinuousAxisEdgeY();
                    }
                    var axisTitleGroup = elem.svg.append("svg:g")
                        .attr("class", "axis");
                    addXaxisTitle(axisTitleGroup, xTitle);
                    addYaxisTitle(axisTitleGroup, yTitle);
                    addxAxisHelp(axisTitleGroup, xTitle);
                    addyAxisHelp(axisTitleGroup, yTitle);  
                },
                getXHelp: function() {
                    return xTitleHelp;
                },
                getYHelp: function() {
                    return yTitleHelp;
                },
                updateLogScaleX: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-x-axis-class").remove();
                    d3.select("#plots_box").select(".x-axis-title").remove();
                    d3.select("#plots_box").select(".x-title-help").remove();
                    var _dataAttr = OneGeneDataProxy.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_x <= (Plots.getLogScaleThreshold())) {
                            var min_x = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
                        } else {
                            var min_x = Math.log(_dataAttr.min_x) / Math.log(2);
                        }
                        if (_dataAttr.max_x <= (Plots.getLogScaleThreshold())) {
                            var max_x = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
                        } else {
                            var max_x = Math.log(_dataAttr.max_x) / Math.log(2);
                        }
                        var edge_x = (max_x - min_x) * 0.2;
                        attr.xScale = d3.scale.linear()
                            .domain([min_x - edge_x, max_x + edge_x])
                            .range([100, 600]);
                        xAxis = d3.svg.axis()
                            .scale(attr.xScale)
                            .orient("bottom")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle + "(log2)");
                        addxAxisHelp(axisTitleGroup, xTitle + "(log2)");
                    } else {
                        initContinuousAxisX();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addXaxisTitle(axisTitleGroup, xTitle);
                        addxAxisHelp(axisTitleGroup, xTitle);
                    }
                    drawContinuousAxisMainX();
                },
                updateLogScaleY: function(applyLogScale) {
                    d3.select("#plots_box").select(".plots-y-axis-class").remove();
                    d3.select("#plots_box").select(".y-axis-title").remove();
                    d3.select("#plots_box").select(".y-title-help").remove();
                    var _dataAttr = OneGeneDataProxy.getDataAttr();
                    if (applyLogScale) {
                        if (_dataAttr.min_y <= (Plots.getLogScaleThreshold())) {
                            var min_y = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
                        } else {
                            var min_y = Math.log(_dataAttr.min_y) / Math.log(2);
                        }
                        if (_dataAttr.max_y <= (Plots.getLogScaleThreshold())) {
                            var max_y = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
                        } else {
                            var max_y = Math.log(_dataAttr.max_y) / Math.log(2);
                        }
                        var edge_y = (max_y - min_y) * 0.1;
                        attr.yScale = d3.scale.linear()
                            .domain([min_y - edge_y, max_y + edge_y])
                            .range([520, 20]);
                        yAxis = d3.svg.axis()
                            .scale(attr.yScale)
                            .orient("left")
                            .tickSize(6, 0, 0)
                            .tickPadding([8]);
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle + "(log2)");
                        addyAxisHelp(axisTitleGroup, yTitle + "(log2)");
                    } else {
                        initContinuousAxisY();
                        var axisTitleGroup = elem.svg.append("svg:g")
                            .attr("class", "axis");
                        addYaxisTitle(axisTitleGroup, yTitle);
                        addyAxisHelp(axisTitleGroup, yTitle);
                    }
                    drawContinuousAxisMainY();
                }
            };
        }());

        var Qtips = (function() {

            function confContent(d) {
                var content = "<font size='2'>";
                if (OneGeneUtil.plotsTypeIsCopyNo()) {
                    if (OneGeneUtil.plotsIsDiscretized()) {
                        content += "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    } else {
                        content += "CNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                            "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"+
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            +"' target = '_blank'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (OneGeneUtil.plotsTypeIsMethylation()) {
                    content += "Methylation: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "mRNA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid") {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                } else if (OneGeneUtil.plotsTypeIsRPPA()) {
                    content += "mRNA: <strong>" + parseFloat(d.xVal).toFixed(3) + "</strong><br>" +
                        "RPPA: <strong>" + parseFloat(d.yVal).toFixed(3) + "</strong><br>";
                    if (d.gisticType !== "Diploid") {
                        content = content + "CNA: " + "<strong>" + d.gisticType + "</strong><br>";
                    }
                    content += "Case ID: <strong><a href='"
                            +cbio.util.getLinkToSampleView(cancer_study_id,d.caseId)
                            + "'>" + d.caseId +
                        "</a></strong><br>";
                    if (d.mutationType !== 'non') {
                        content = content + "Mutation: " + "<strong>" + d.mutationDetail.replace(/,/g, ", ") + "<br>";
                    }
                }
                content = content + "</font>";
                return content;
            }

            return {
                init: function(){
                    elem.elemDotsGroup.selectAll("path").each(
                        function(d) {
                            var content = confContent(d);
                            $(this).qtip(
                                {
                                    content: {text: content},
                                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                    show: {event: "mouseover"},
                                    hide: {fixed:true, delay: 100, event: "mouseout"},
                                    position: {my:'left bottom',at:'top right', viewport: $(window)}
                                }
                            );
                            if (OneGeneUtil.plotsTypeIsCopyNo()) {    //Handle special symbols
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200)
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol()
                                            .size(20)
                                            .type(function(d){
                                                return mutationStyle[d.mutationType].symbol;
                                            })
                                        )
                                        .attr("fill", function(d){
                                            return mutationStyle[d.mutationType].fill;
                                        })
                                        .attr("stroke", function(d){
                                            return mutationStyle[d.mutationType].stroke;
                                        })
                                        .attr("stroke-width", 1.2);
                                };
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
                            } else {
                                var mouseOn = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(200));
                                };
                                var mouseOff = function() {
                                    var dot = d3.select(this);
                                    dot.transition()
                                        .ease("elastic")
                                        .duration(600)
                                        .delay(100)
                                        .attr("d", d3.svg.symbol().size(35));
                                };
                                elem.elemDotsGroup.selectAll("path").on("mouseover", mouseOn);
                                elem.elemDotsGroup.selectAll("path").on("mouseout", mouseOff);
                            }
                        }
                    );
                }
            };
        }());

        var ScatterPlots = (function() {

            function drawDiscretizedPlots() { //GISTIC, RAE view
                var ramRatio = 30;  //Noise
                //Divide Data Set by Gistic Type
                var subDataSet = {
                    Homdel : [],
                    Hetloss : [],
                    Diploid : [],
                    Gain : [],
                    Amp : []
                };
                $.each(OneGeneDataProxy.getDotsGroup(), function(index, value) {
                    if (value.gisticType === "Homdel") {
                        subDataSet.Homdel.push(value);
                    } else if (value.gisticType === "Hetloss") {
                        subDataSet.Hetloss.push(value);
                    } else if (value.gisticType === "Diploid") {
                        subDataSet.Diploid.push(value);
                    } else if (value.gisticType === "Gain") {
                        subDataSet.Gain.push(value);
                    } else if (value.gisticType === "Amp") {
                        subDataSet.Amp.push(value);
                    }
                });
                //Remove empty data set
                $.each(subDataSet, function(key, value) {
                    if (subDataSet[key].length === 0) {
                        delete subDataSet[key];
                    }
                });
                var posVal = 0;    //Index for Positioning
                $.each(subDataSet, function(key, value) {
                    var subDotsGrp = elem.elemDotsGroup.append("svg:g");
                    subDotsGrp.selectAll("path")
                        .data(value)
                        .enter()
                        .append("svg:path")
                        .attr("class", function(d){ return d.caseId;})
                        .attr("transform", function(d){
                            var _x = attr.xScale(posVal) + (Math.random() * ramRatio - ramRatio/2);
                            var _y = attr.yScale(d.yVal);
                            $(this).attr("x_pos", _x);
                            $(this).attr("y_pos", _y);
                            $(this).attr("x_val", d.xVal);
                            $(this).attr("y_val", d.yVal);
                            $(this).attr("size", 20);
                            return "translate(" + _x + "," + _y + ")";
                        })
                        .attr("d", d3.svg.symbol()
                            .size(20)
                            .type(function(d){
                                $(this).attr("symbol", mutationStyle[d.mutationType].symbol);
                                return mutationStyle[d.mutationType].symbol;
                            })
                        )
                        .attr("fill", function(d){
                            $(this).attr("fill", mutationStyle[d.mutationType].fill);
                            return mutationStyle[d.mutationType].fill;
                        })
                        .attr("stroke", function(d){
                            $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                            return mutationStyle[d.mutationType].stroke;
                        })
                        .attr("stroke-width", 1.2);
                    posVal += 1;
                });
            }

            function drawBoxPlots(applyLogScale){
                d3.select("#plots_box").select(".box_plots").remove();
                var boxPlotsElem = elem.boxPlots.append("svg:g").attr("class", "box_plots");
                var _dotsGroup = [];
                _dotsGroup.length = 0;
                _dotsGroup = jQuery.extend(true, {}, OneGeneDataProxy.getDotsGroup());
                if (applyLogScale) {
                    $.each(_dotsGroup, function(index, value) {
                        if (value.yVal <= (Plots.getLogScaleThreshold())) {
                            value.yVal = Math.log(Plots.getLogScaleThreshold()) / Math.log(2);
                        } else {
                            value.yVal = Math.log(value.yVal) / Math.log(2);
                        }
                    });
                }

                var min_x = OneGeneDataProxy.getDataAttr().min_x;
                var max_x = OneGeneDataProxy.getDataAttr().max_x;

                //Not using real x value for positioning discretized data
                var pos = 0;   //position Indicator
                for (var i = min_x ; i < max_x + 1; i++) {
                    //TODO: fix the pos (increase when data available only)
                    var boxPlot = new BoxPlots();
                    var _tmpArr = [];
                    $.each(_dotsGroup, function(index, value) {
                        if (value.xVal === i.toString()) {
                            _tmpArr.push(parseFloat(value.yVal));
                        }
                    });
                    if (_tmpArr.length !== 0) {
                        boxPlot.init(attr, _tmpArr, boxPlotsElem, pos, i);
                        pos += 1;                        
                    }
                }
            }

            function drawLog2Plots() {
                elem.elemDotsGroup.selectAll("path")
                    .attr("class", "dots")
                    .data(OneGeneDataProxy.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d) {
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 20);
                        return "translate(" + _x + ", " + _y + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d){
                            return mutationStyle[d.mutationType].symbol;
                        })
                    )
                    .attr("fill", function(d){
                        $(this).attr("fill", mutationStyle[d.mutationType].fill);
                        return mutationStyle[d.mutationType].fill;
                    })
                    .attr("stroke", function(d){
                        $(this).attr("stroke", mutationStyle[d.mutationType].stroke);
                        return mutationStyle[d.mutationType].stroke;
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId});
            }

            function drawContinuousPlots() {  //RPPA, DNA Methylation Views
                elem.elemDotsGroup.selectAll("path")
                    .data(OneGeneDataProxy.getDotsGroup())
                    .enter()
                    .append("svg:path")
                    .attr("transform", function(d){
                        var _x = attr.xScale(d.xVal);
                        var _y = attr.yScale(d.yVal);
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        $(this).attr("x_val", d.xVal);
                        $(this).attr("y_val", d.yVal);
                        $(this).attr("symbol", "circle");
                        $(this).attr("size", 35);
                        return "translate(" + attr.xScale(d.xVal) + ", " + attr.yScale(d.yVal) + ")";
                    })
                    .attr("d", d3.svg.symbol()
                        .size(35)
                        .type("circle"))
                    .attr("fill", function(d) {
                        switch (d.mutationType) {
                            case "non" : {$(this).attr("fill", "white");return "white";}
                            default: {$(this).attr("fill", "orange");return "orange";}
                        }
                    })
                    .attr("fill-opacity", function(d) {
                        switch (d.mutationType) {
                            case "non" : return 0.0;
                            default : return 1.0;
                        }
                    })
                    .attr("stroke", function(d) {
                        return gisticStyle[d.gisticType].stroke;
                    })
                    .attr("stroke-width", 1.2)
                    .attr("class", function(d) { return d.caseId; });
            }

            return {
                init: function() {
                    elem.boxPlots = elem.svg.append("svg:g");
                    elem.elemDotsGroup = elem.svg.append("svg:g");
                    if (OneGeneUtil.plotsTypeIsCopyNo()) {
                        if (OneGeneUtil.plotsIsDiscretized()) {    //Gistic, RAE...
                            drawBoxPlots(false);
                            drawDiscretizedPlots();
                        } else {   //Log2
                            drawLog2Plots();
                        }
                    } else {  //Methylation, RPPA
                        drawContinuousPlots();
                    }
                },
                updateLogScaleX: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            if (applyLogScale) {
                                if(d3.select(this).attr("x_val") <= (Plots.getLogScaleThreshold())) {
                                    var _post_x = attr.xScale(Math.log(Plots.getLogScaleThreshold()) / Math.log(2));
                                } else {
                                    var _post_x = attr.xScale(Math.log(d3.select(this).attr("x_val")) / Math.log(2));
                                }
                            } else {
                                var _post_x = attr.xScale(d3.select(this).attr("x_val"));
                            }
                            var _pre_y = d3.select(this).attr("y_pos");
                            d3.select(this).attr("x_pos", _post_x);
                            return "translate(" + _post_x + ", " + _pre_y + ")";
                        });
                },
                updateLogScaleY: function(applyLogScale) {
                    elem.elemDotsGroup.selectAll("path")
                        .transition().duration(300)
                        .attr("transform", function() {
                            var _pre_x = d3.select(this).attr("x_pos");
                            if (applyLogScale) {
                                if (parseFloat(d3.select(this).attr("y_val")) <= (Plots.getLogScaleThreshold())) {
                                    var _post_y = attr.yScale(Math.log(Plots.getLogScaleThreshold()) / Math.log(2));
                                } else {
                                    var _post_y = attr.yScale(Math.log(d3.select(this).attr("y_val")) / Math.log(2));
                                }
                            } else {
                                var _post_y = attr.yScale(d3.select(this).attr("y_val"));
                            }
                            d3.select(this).attr("y_pos", _post_y);
                            return "translate(" + _pre_x + ", " + _post_y + ")";
                        });
                    if (OneGeneUtil.plotsIsDiscretized()) {
                        drawBoxPlots(applyLogScale);
                    }
                }
            }
        }());

        var Legends = (function() {

            function drawCopyNoViewLegends() {
                //Only show glyphs whose mutation type
                //appeared in the current individual case
                var _appearedMutationTypes = [];
                _appearedMutationTypes.length = 0;
                $.each(OneGeneDataProxy.getDotsGroup(), function(index, value) {
                    _appearedMutationTypes.push(value.mutationType);
                });

                //Convert object to array
                var mutationStyleArr = [];
                mutationStyleArr.length = 0;
                for (var key in mutationStyle) {
                    var obj = mutationStyle[key];
                    if (_appearedMutationTypes.indexOf(key) !== -1) {
                        mutationStyleArr.push(obj);
                    }
                }
                //If only contain non mutation cases, remove the glyphs completely
                if (mutationStyleArr.length === 1 &&
                    mutationStyleArr[0].legendText === mutationStyle["non"].legendText) {
                    mutationStyleArr.length = 0;
                }

                var legend = elem.svg.selectAll(".legend")
                    .data(mutationStyleArr)
                    .enter().append("svg:g")
                    .attr("transform", function(d, i) {
                        return "translate(610, " + (30 + i * 15) + ")";
                    });

                legend.append("path")
                    .data(mutationStyleArr)
                    .attr("width", 18)
                    .attr("height", 16)
                    .attr("d", d3.svg.symbol().size(30)
                        .type(function(d) {return d.symbol;}))
                    .attr("fill", function(d) {return d.fill;})
                    .attr("stroke", function(d){return d.stroke;})
                    .attr("stroke-width", 1.2);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d){return d.legendText;});
            }

            function drawOtherViewLegends() {
                var gisticStyleArr = [];
                for (var key in gisticStyle) {
                    var obj = gisticStyle[key];
                    gisticStyleArr.push(obj);
                }

                var mutatedStyle = {
                    stroke : "none",
                    symbol : "circle",
                    fill : "orange",
                    legendText : "Mutated"
                }
                gisticStyleArr.push(mutatedStyle);

                var legend = elem.svg.selectAll(".legend")
                    .data(gisticStyleArr)
                    .enter().append("g")
                    .attr("class", "legend")
                    .attr("transform", function(d, i) {
                        return "translate(610, " + (30 + i * 15) + ")";
                    })

                legend.append("path")
                    .attr("width", 18)
                    .attr("height", 18)
                    .attr("d", d3.svg.symbol()
                        .size(40)
                        .type(function(d) { return d.symbol; }))
                    .attr("fill", function (d) { return d.fill; })
                    .attr("stroke", function (d) { return d.stroke; })
                    .attr("stroke-width", 1.2);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d) { return d.legendText; })
            }

            return {
                init: function() {
                    if (OneGeneUtil.plotsTypeIsCopyNo()) {
                        drawCopyNoViewLegends();
                    } else {
                        drawOtherViewLegends();
                    }
                    if (!OneGeneUtil.plotsIsDiscretized()) {
                        var tmpDataAttr = OneGeneDataProxy.getDataAttr();
                        var tmpPearson = "Pearson: " + tmpDataAttr.pearson,
                            tmpSpearman = "Spearman: " + tmpDataAttr.spearman;
                        var coExpLegend = elem.svg.selectAll(".coexp_legend")
                            .data(["Correlation", tmpPearson, tmpSpearman])
                            .enter().append("g")
                            .attr("class", "coexp_legend")
                            .attr("transform", function(d, i) {
                                return "translate(600, " + (150 + i * 15) + ")";
                            });
                        coExpLegend.append("text")
                                .attr("dx", ".75em")
                                .attr("dy", ".35em")
                                .style("text-anchor", "front")
                                .text(function(d) {
                                    return d;
                                });                      
                    }
                }
            }
        }());

        function initCanvas() {
            elem.svg = d3.select("#plots_box")
                .append("svg")
                .attr("width", settings.canvas_width)
                .attr("height", settings.canvas_height);
        }

        function drawErrMsgs() {
            var _xDataType = "",
                _yDataType = "";
            if (OneGeneUtil.plotsTypeIsCopyNo()) {
                var e = document.getElementById("data_type_copy_no");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (OneGeneUtil.plotsTypeIsMethylation()) {
                var e = document.getElementById("data_type_dna_methylation");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_mrna");
                _yDataType = e.options[e.selectedIndex].text;
            } else if (OneGeneUtil.plotsTypeIsRPPA()) {
                var e = document.getElementById("data_type_mrna");
                _xDataType = e.options[e.selectedIndex].text;
                e = document.getElementById("data_type_rppa");
                _yDataType = e.options[e.selectedIndex].text;
            }

            var err_line1 = "There is no UNAVAILABLE_DATA_TYPE data";
            var err_line2 = "for " + userSelection.gene + " in the selected cancer study.";
            var _dataStatus = OneGeneDataProxy.getDataStatus();
            if (!_dataStatus.xHasData && _dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _xDataType);
            } else if (_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", _yDataType);
            } else if (!_dataStatus.xHasData && !_dataStatus.yHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "both selected data types");
            } else if (_dataStatus.xHasData &&_dataStatus.yHasData &&
                !_dataStatus.combineHasData) {
                err_line1 = err_line1.replace("UNAVAILABLE_DATA_TYPE", "combined data types");
            }

            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 50)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line1)
            elem.svg.append("text")
                .attr("x", 350)
                .attr("y", 70)
                .attr("text-anchor", "middle")
                .attr("fill", "#DF3A01")
                .text(err_line2)
            elem.svg.append("rect")
                .attr("x", 150)
                .attr("y", 30)
                .attr("width", 400)
                .attr("height", 50)
                .attr("fill", "none")
                .attr("stroke-width", 1)
                .attr("stroke", "#BDBDBD");
        }

        function drawImgConverter() {
            $('#view_title').empty();
            if (OneGeneUtil.plotsTypeIsCopyNo()) {
                $('#view_title').append(userSelection.gene + ": mRNA Expression v. CNA ");
            } else if (OneGeneUtil.plotsTypeIsMethylation()) {
                $('#view_title').append(userSelection.gene + ": mRNA Expression v. DNA Methylation ");
            } else if (OneGeneUtil.plotsTypeIsRPPA()) {
                $('#view_title').append(userSelection.gene + ": RPPA protein level v. mRNA Expression ");
            }
            var pdfConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank' " +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
            $('#view_title').append(pdfConverterForm);
            var svgConverterForm = "<form style='display:inline-block' action='svgtopdf.do' method='post' target='_blank'" +
                "onsubmit=\"this.elements['svgelement'].value=loadPlotsSVG();\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='correlation_plot-" + userSelection.gene + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
            $('#view_title').append(svgConverterForm);
        }

        function applyLogScaleX(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleX(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleX(applyLogScale);
        }

        function applyLogScaleY(applyLogScale) {
            //Update the axis
            Axis.updateLogScaleY(applyLogScale);
            //Update the position of the dots
            ScatterPlots.updateLogScaleY(applyLogScale);
        }

        return {
            init: function() {
                initCanvas();
                if (OneGeneDataProxy.getDotsGroup().length !== 0) {
                    drawImgConverter();
                    Axis.init();
                    ScatterPlots.init();
                    Legends.init();
                    Qtips.init();
                    if (document.getElementById("log_scale_option_x") !== null) {
                        var _applyLogScaleX = document.getElementById("log_scale_option_x").checked;
                        applyLogScaleX(_applyLogScaleX);
                    }
                    if (document.getElementById("log_scale_option_y") !== null) {
                        var _applyLogScaleY = document.getElementById("log_scale_option_y").checked;
                        applyLogScaleY(_applyLogScaleY);
                    }
                } else { //No available data
                    drawErrMsgs();
                }
            },
            applyLogScaleX: applyLogScaleX,
            applyLogScaleY: applyLogScaleY
        }
    }());

    var View = (function() {
    
        function configSettings() {

        }
    
    }());

    function getDataCallback() {
        console.log(OneGeneDataProxy.getDotsGroup());
        console.log(OneGeneDataProxy.getDataAttr());
        console.log(OneGeneDataProxy.getDataStatus());
        View.init();        
    }

    return {
        init: function(){
            $('#plots_box').empty();
            $('#loading-image').show();
            OneGeneDataProxy.init(getDataCallback);
        },
        applyLogScaleX: function() {
            var applyLogScale = document.getElementById("log_scale_option_x").checked;
            View.applyLogScaleX(applyLogScale);
        },
        applyLogScaleY: function() {
            var applyLogScale = document.getElementById("log_scale_option_y").checked;
            View.applyLogScaleY(applyLogScale);
        }
    };

}());//Closing OneGene (plots view)


