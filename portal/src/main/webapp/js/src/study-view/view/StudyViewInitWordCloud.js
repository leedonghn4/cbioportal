/*
 * This is the implementation of d3-cloud coming from Jason Davies

The following statment is coming from the Lisense of Jason Davies's d3-cloud

Copyright (c) 2013, Jason Davies.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * The name Jason Davies may not be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JASON DAVIES BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

var StudyViewInitWordCloud = (function() {
    //The length of words should be same with the length of fontSize.
    var words = [],
        fontSize = [],
        percentage = [];
    var WIDTH = 150,
        HEIGHT = 150;
    
    var initStatus = false;
    
    function initData(_data){
        words = _data.names;
        fontSize = _data.size;
        percentage = _data.percentage;
    }
    
    var DIV = {
        mainDiv : "study-view-word-cloud-main",
        titleDiv: "study-view-word-cloud-title",
        chartDiv : "study-view-word-cloud"
    };

    function addQtip() {
        $('#' + DIV.chartDiv + '-download-icon').qtip('destroy', true);
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip('destroy', true);
        
        //Add qtip for download icon when mouse over
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip({
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseout"},
            position: {my:'bottom left',at:'top right', viewport: $(window)},
            content: {
                text:   "Download"
            }
        });
        
        //Add qtip for download icon when mouse click
        $('#' + DIV.chartDiv + '-download-icon').qtip({
            id: '#' + DIV.chartDiv + "-download-icon-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "click", delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseout"},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:   "<form style='display:inline-block;float:left;margin: 0 2px' action='svgtopdf.do' method='post' id='"+DIV.chartDiv+"-pdf'>"+
                        "<input type='hidden' name='svgelement' id='"+DIV.chartDiv+"-pdf-value'>"+
                        "<input type='hidden' name='filetype' value='pdf'>"+
                        "<input type='hidden' id='"+DIV.chartDiv+"-pdf-name' name='filename' value='"+StudyViewParams.params.studyId + "_word_cloud.pdf'>"+
                        "<input type='submit' style='font-size:10px;' value='PDF'>"+          
                        "</form>"+
                        "<form style='display:inline-block;float:left;margin: 0 2px' action='svgtopdf.do' method='post' id='"+DIV.chartDiv+"-svg'>"+
                        "<input type='hidden' name='svgelement' id='"+DIV.chartDiv+"-svg-value'>"+
                        "<input type='hidden' name='filetype' value='svg'>"+
                        "<input type='hidden' id='"+DIV.chartDiv+"-svg-name' name='filename' value='"+StudyViewParams.params.studyId + "_word_cloud.svg'>"+
                        "<input type='submit' style='font-size:10px;clear:right;float:right;' value='SVG'></form>"
            },
            events: {
                show: function() {
                    $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip('api').hide();
                },
                render: function(event, api) {
                    $("#"+DIV.chartDiv+"-pdf", api.elements.tooltip).submit(function(){
                        setSVGElementValue(DIV.chartDiv,
                            DIV.chartDiv+"-pdf-value");
                    });
                    $("#"+DIV.chartDiv+"-svg", api.elements.tooltip).submit(function(){
                        setSVGElementValue(DIV.chartDiv,
                            DIV.chartDiv+"-svg-value");
                    });
                }
            }
        });
    }

    function initDiv(){
        $("#study-view-charts").append(StudyViewBoilerplate.wordCloudDiv);
        $("#study-view-word-cloud-pdf-name").val("Word_Cloud_"+ StudyViewParams.params.studyId +".pdf");
        $("#study-view-word-cloud-svg-name").val("Word_Cloud_"+ StudyViewParams.params.studyId +".svg");
    }
    
    //Add all listener events
    function addEvents() {
        StudyViewUtil.showHideTitle(
            "#"+DIV.mainDiv, 
            "#"+DIV.chartDiv+"-header",
            0,
            "Mutated Genes",
            30,
            30
        );
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue){
        var svgElement;
        
        //Remove x/y title help icon first.
        svgElement = $("#" + _svgParentDivId + " svg").html();
        $("#" + _idNeedToSetValue)
                .val("<svg width='200' height='200'>"+
                    "<g><text x='100' y='20'  style='font-weight: bold; "+
                    "text-anchor: middle'>"+
                    "Mutated Genes</text></g><g transform='translate(10, 20)'>"+
                    svgElement + "</g></svg>");
    }
    
    //This function is inspired by Jason's daw function.
    function draw(words){
        var fill = d3.scale.category20();
        var startX = 0, startY = 0;
        

        var _svg = d3.select("#study-view-word-cloud").append("svg")
            .attr("width", WIDTH)
            .attr("height", HEIGHT);

        var _g = _svg.append("g")
            .attr("transform", "translate(10,40)");

        _g.selectAll("text")
            .data(words)
          .enter().append("text")
            .style("font-size", function(d) { return d.size + "px"; })
            .style("font-family", "Impact")
            .style("fill", 'green')
            .style('cursor', 'pointer')
            .attr("transform", function(d, i) {
                //TODO: A constant didider for width and height of each text 
                //are calculated based on multiple testing. This should be
                //changed later.
                var _translate = "translate(" + [startX, startY] + ")";
                var _width = (d.width * (3 / 4));
                
                startX += _width;
                
                if(startX > (WIDTH-10)){
                    startX = 0;
                    startY += d.y1 * 1.7;
                    _translate = "translate(" + [startX, startY] + ")";
                    startX += _width;
                }
                
                return _translate;
            })
            .text(function(d) { return d.text; });

        startX = 0;
        startY = 0;
        _g.selectAll("rect")
            .data(words)
          .enter().append("rect")
            .attr("fill", "lightgrey")
            .attr("width", "30px")
            .attr("height", "6px")
            .attr("transform", function(d, i) {
                var _translate = "translate(" + [startX, startY+2] + ")";
                var _width = (d.width * (3 / 4));
                
                startX += _width;
                
                if(startX > (WIDTH-10)){
                    startX = 0;
                    startY += d.y1 * 1.7;
                    _translate = "translate(" + [startX, startY+2] + ")";
                    startX += _width;
                }
                return _translate;
            });

        startX = 0;
        startY = 0;
        _g.selectAll()
        .data(words)
          .enter().append("rect")
            .attr("fill", "red")
            .attr("width", function(d, i) {
                return (Number(d.percentage) * 30) + "px";
            })
            .attr("height", "6px")
            .attr("transform", function(d, i) {
                var _translate = "translate(" + [startX, startY+2] + ")";
                var _width = (d.width * (3 / 4));
                
                startX += _width;
                
                if(startX > (WIDTH-10)){
                    startX = 0;
                    startY += d.y1 * 1.7;
                    _translate = "translate(" + [startX, startY+2] + ")";
                    startX += _width;
                }
                
                return _translate;
            });
      
        $("#study-view-word-cloud svg text").click(function(){
            var _text = $(this).text();
            window.open("index.do?Action=Submit&"+
                        "genetic_profile_ids="+StudyViewParams.params.mutationProfileId+"&" +
                        "case_set_id="+StudyViewParams.params.caseSetId+"&" +
                        "cancer_study_id="+StudyViewParams.params.studyId+"&" +
                        "gene_list="+ _text +"&tab_index=tab_visualize&" +
                        "#mutation_details");
        });
    }
    
    //Changed based on Jason's example file.
    function initD3Cloud() {
        d3.layout.cloud().size([180, 180])
            .words(words.map(function(d, index) {
                return {text: d, size: fontSize[index], percentage: percentage[index]};
            }))
            .padding(0)
            .rotate(function() { return ~~0; })
            .font("Impact")
            .fontSize(function(d) { return d.size; })
            .on("end", draw)
            .start();
    }
    
    function redraw(_data){
        $("#study-view-word-cloud").find('svg').remove();
        initData(_data);
        initD3Cloud();
    }
    
    return {
        init: function(_data){
            initData(_data);
            initDiv();
            initD3Cloud();
            addQtip();
            addEvents();
            initStatus = true;
        },
        
        redraw: redraw,
        getInitStatus: function(){
            return initStatus;
        }
    };
})();
