//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

/*
 * @author  Hongxin Zhang
 * @date    Apr. 2014
 */

/*
 * 
 * Save curve function has been disabled.
 */




var StudyViewSurvivalPlotView = (function() {
    var oData = [], //The data before processing, orginal data
        oDataLength = 0,
        aData = {}, //The data after processing
        inputArr = [],
        survivalPlot = {},
        kmEstimator = "",
        logRankTest = "",
        plotsInfo = {},
        numOfPlots = 0,
        opts = [],
        // if survival plot has been initialized, the status will be set to true.
        initStatus = false;

    var curveInfo = {};

    function getInitStatus() {
        return initStatus;
    }
    
    /**
     * Containing all jQuery related functions
     * @param {type} _plotKey the plot key
     */
    function addEvents(_plotKey) {
        var _opts = opts[_plotKey],
            _title = $("#" + _opts.divs.main + " charttitleh4").text();

        StudyViewUtil.showHideTitle(
            '#' + _opts.divs.main,
            '#' + _opts.divs.header,
            0, _title, 60, 70 );

        $("#" + _opts.divs.body).css('opacity', '1');
        $("#" + _opts.divs.loader).css('display', 'none');
        
        $('#' + _opts.divs.downloadIcon).qtip('destroy', true);
        $('#' + _opts.divs.downloadIcon).qtip({
            id: "#" + _opts.divs.downloadIcon + "-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "click", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:   "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opts.divs.pdf + "'>" +
                        "<input type='hidden' name='svgelement' id='" + _opts.divs.pdfValue + "'>" +
                        "<input type='hidden' name='filetype' value='pdf'>" +
                        "<input type='hidden' id='" + _opts.divs.pdfName + "' name='filename' value=''>" +
                        "<input type='submit' style='font-size:10px' value='PDF'>" +
                        "</form>" +
                        "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opts.divs.svg + "'>" +
                        "<input type='hidden' name='svgelement' id='" + _opts.divs.svgValue + "'>" +
                        "<input type='hidden' name='filetype' value='svg'>" +
                        "<input type='hidden' id='" + _opts.divs.svgName + "' name='filename' value=''>" +
                        "<input type='submit' style='font-size:10px' value='SVG'>" +
                        "</form>"
            },
            events: {
                render: function(event, api) {

                    $("#" + _opts.divs.pdfName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".pdf");
                    $("#" + _opts.divs.svgName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".svg");
    
                    $("#" + _opts.divs.pdf, api.elements.tooltip).submit(function() {
                        setSVGElementValue(_opts.divs.bodySvg,
                                _opts.divs.pdfValue, _plotKey, _title);
                    });
                    $("#" + _opts.divs.svg, api.elements.tooltip).submit(function() {
                        setSVGElementValue(_opts.divs.bodySvg,
                                _opts.divs.svgValue, _plotKey, _title);
                    });
                }
            }
        });
    }  
        
    function createPvalMatrix(_plotKey, _curveInfo) {
        var _input = [],
            _numOfCurves = _curveInfo.length,
            _curvePairs = [];
    
        for(var i = 0; i < _numOfCurves; i++) {
            for(var j = i+1; j < _numOfCurves; j++) {
                _curvePairs.push(_curveInfo[i].name + " ~ " + _curveInfo[j].name);
                _input.push([_curveInfo[i].data.data.getData(), _curveInfo[j].data.data.getData()]);
            }
        }
        
        var _callback = function(_data) {
            var _content = 0;
            
            if(isNaN(_data)) {
                _content = "NA";
            } else {
                if(_data < 0.001) {
                    _content = "< 0.001";
                } else {
                    _content = parseFloat(_data).toFixed(3);
                }
            }
            
            $("#" + opts[_plotKey].divs.pvalMatrix).empty();
            $("#" + opts[_plotKey].divs.pvalMatrix)
                .append("<div style='height: 15px'><svg style='width: 10px;margin-right: 5px;float:left'><rect width=10 height=10 fill="+
                        _curveInfo[0].color+"></rect></svg><span style='font-weight:bold;font-size:10px;color:#000000'>"+
                        _curveInfo[0].name+"</span></div>");
            
            $("#" + opts[_plotKey].divs.pvalMatrix)
                .append("<div style='height: 15px'><svg style='width: 10px;margin-right: 5px;float:left'><rect width=10 height=10 fill="+
                        _curveInfo[1].color+"></rect></svg><span style='font-weight:bold;font-size:10px;color:#000000'>"+
                        _curveInfo[1].name+"</span></div>");
            
            $("#" + opts[_plotKey].divs.pvalMatrix)
                .append("<div style='height: 15px'><span>Logrank Test P-Value: "+_content+"</span></div>");
            
            addPvalQtip(_plotKey);
        };
        
        var _listCallback = function(_data) {
            var _pvalueList = _data['pvalueList'].split(/[,\s+]/);
            
            $("#" + opts[_plotKey].divs.pvalMatrix).empty();
            
            $("#" + opts[_plotKey].divs.pvalMatrix).append("<span style='font-weight:bold;font-size:12px'>Logrank Test P-Value</span><br/><br/>");
            
            var _table = $('<table />', {attr: {'class':'pvalMatixTable'}, css: {"text-align": 'left'}});
            var _tbody = $('<tbody />');
            var _pairIndex = 0;
            for(var i = 0; i < _numOfCurves; i++) {
                var _content = "<svg style='width: 10px;margin-right: 5px;float:left'><rect width=10 height=10 fill="+
                        _curveInfo[i].color+"></rect></svg><span style='font-weight:bold;font-size:10px;color:#000000'>"+
                        _curveInfo[i].name+"</span>";
                var _tr = $('<tr />');
                var _img = $('<img />', {css: {width: '8px'}, src: 'images/close.svg'});
                var _td = $('<td />');
                _td.append(_img);
                _tr.append(_td);
                _tr.append($('<td>'+_content+'</td>'));
                
                for(var j = 0; j < _numOfCurves-1; j++) {
                    var _td = "";
                    if( j >= i ) {
                        _td = $('<td />', {text: ''});
                    }else if( j < i ) {
                        var _distance = 0;
                        for(var z = 0; z < j; z++) {
                            _distance += _numOfCurves-2-z;
                        }
                        var _index = (i-1) + _distance;
                        
                        var _content = parseFloat(_pvalueList[_index]);
                        var _bacgroundColor = '#FFFFFF';
                        var _color = '#000000';
                        if(_content < 0.001) {
                            _content = "< 0.001";
                            _bacgroundColor = "#FF3300";
                            _color = "#FFFFFF";
                        }else if(!isNaN(_content)){
                            _content = _content.toFixed(3);
                            if(_content < 0.05) {
                                _bacgroundColor = "#FFcc00";
                            }
                        }else {
                            _content = "NA";
                        }
                        _td = $('<td />', {text: _content, css: {'color':_color,'background-color':_bacgroundColor}});
                    }else {
                        var _content = parseFloat(_pvalueList[_pairIndex]);
                        var _bacgroundColor = '#FFFFFF';
                        var _color = '#000000';
                        if(_content < 0.001) {
                            _content = "< 0.001";
                            _bacgroundColor = "#FF3300";
                            _color = "#FFFFFF";
                        }else if(!isNaN(_content)){
                            _content = _content.toFixed(3);
                            if(_content < 0.05) {
                                _bacgroundColor = "#FFcc00";
                            }
                        }else {
                            _content = "NA";
                        }
                        _td = $('<td />', {text: _content, css: {'color':_color,'background-color':_bacgroundColor}});
                        _pairIndex++;
                    }
                    _tr.append(_td);
                }
                
                _tbody.append(_tr);
            }
            _table.append(_tbody);
            
            var _tfoot = $('<tfoot />');
        
            _tfoot.append($("<th />"));
            _tfoot.append($("<th />"));
            for(var i = 0; i < _numOfCurves-1; i++) {
                var _content = "<svg style='width: 10px;'><rect width=10 height=10 fill="+_curveInfo[i].color+"></rect></svg>";
                var _th = $("<th style='text-align: center'> "+_content+"</th>");
                _tfoot.append(_th);
            }
            _table.append(_tfoot);
            
            $("#" + opts[_plotKey].divs.pvalMatrix).append(_table);
            $("#" + opts[_plotKey].divs.pvalIconWrapper).css('display', 'block');
            addPvalQtip(_plotKey);
        };
                
        var _logRankTest = new LogRankTest();
        if(_curvePairs.length > 1) {
            _logRankTest.calcList(_plotKey, _input, _listCallback);
        }else if (_curvePairs.length === 1) {
            _logRankTest.calc(_input[0][0], _input[0][1], _callback);
        }
    }
    
    function addPvalQtip(_plotKey) {
        $("#" + opts[_plotKey].divs.pvalMatrix).css('display', 'none');
        
        $("#" + opts[_plotKey].divs.main + " svg").qtip({
            id: opts[_plotKey].divs.bodyLabel + "-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow forceZindex'},
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left top',at:'top right', viewport: $(window)},
            content:$("#" + opts[_plotKey].divs.pvalMatrix).html(),
            events: {
                render: function(event, api) {
                    $('img', api.elements.tooltip).hover(function() {
                        $(this).css('cursor', 'pointer');
                    });

                    $('img', api.elements.tooltip).unbind('click');
                    $('img', api.elements.tooltip).click(function() {
                            var _parent = $(this).parent(),
                                _color = _parent.parent().find('rect').attr('fill'),
                                _index = _parent.parent().index();
                            $('table td', api.elements.tooltip).remove(":nth-child("+(_index+3)+")");
                            $('table tfoot th', api.elements.tooltip).remove(":nth-child("+(_index+3)+")");
                            _parent.parent().remove();
                            removeCurveFunc(_index, _plotKey);
                            survivalPlot[_plotKey].removeCurve(_color.toString().substring(1) + "-" + _plotKey);
                    });

                    $('svg rect', api.elements.tooltip).hover(function() {
                        $(this).css('cursor', 'pointer');
                    });

                    $('svg rect', api.elements.tooltip).unbind('click');
                    $('svg rect', api.elements.tooltip).click(function() {
                        var _text = $($(this).parent().parent()).find('span'),
                                _rgbRect = StudyViewUtil.rgbStringConvert($(this).css('fill')),
                                _rgbText = StudyViewUtil.rgbStringConvert($(_text).css('color')),
                                _rectColor = StudyViewUtil.rgbToHex(_rgbRect[0], _rgbRect[1], _rgbRect[2]),
                                _textColor = StudyViewUtil.rgbToHex(_rgbText[0], _rgbText[1], _rgbText[2]);
                        
                        if (_textColor === '#000000') {
                            $(_text).css('color', 'red');
                            highlightCurve(_rectColor.substring(1) + "-" + _plotKey);
                        } else {
                            $(_text).css('color', 'black');
                            resetCurve(_rectColor.substring(1) + "-" + _plotKey);
                        }
                    });
                }
            }
        });
    }
    /**
     * Be used to create svg/pdf file
     * @param {type} _svgParentDivId    svg container
     * @param {type} _idNeedToSetValue  set the modified svg element value into
     *                                  this selected element
     * @param {type} _plotKey
     * @param {type} _title             the title appears above saved file 
     *                                  content, Exp. 'Scatter Plot'
     * @returns {undefined}
     */
    function setSVGElementValue(_svgParentDivId, _idNeedToSetValue, _plotKey, _title) {
        var _svgElement, _svgLabels, _svgTitle,
                _labelTextMaxLength = 0,
                _numOfLabels = 0,
                _svgWidth = 360,
                _svgheight = 360;

        _svgElement = $("#" + _svgParentDivId + " svg").html();
        _svgLabels = $("#" + opts[_plotKey].divs.bodyLabel + " svg");

        _svgLabels.find('image').remove();
        _svgLabels.find('text').each(function(i, obj) {
            var _value = $(obj).attr('oValue');

            if (typeof _value === 'undefined') {
                _value = $(obj).text();
            }

            if (_value.length > _labelTextMaxLength) {
                _labelTextMaxLength = _value.length;
            }
            $(obj).text(_value);
            _numOfLabels++;
        });

        _svgWidth += _labelTextMaxLength * 14;

        if (_svgheight < _numOfLabels * 20) {
            _svgheight = _numOfLabels * 20 + 40;
        }

        _svgLabels = _svgLabels.html();

        _svgTitle = "<g><text text-anchor='middle' x='210' y='30' " +
                "style='font-weight:bold'>" + _title + "</text></g>";

        _svgElement = "<svg width='" + _svgWidth + "px' height='" + _svgheight + "px' style='font-size:14px'>" +
                _svgTitle + "<g transform='translate(0,40)'>" +
                _svgElement + "</g><g transform='translate(370,50)'>" +
                _svgLabels + "</g></svg>";
        $("#" + _idNeedToSetValue).val(_svgElement);
        $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();
        drawLabels(_plotKey);
        addPvalQtip(_plotKey);
        //The style has been reset because of the addEvents function, so we
        //need to change the related components manully 
        $("#" + opts[_plotKey].divs.header).css('display', 'block');
        $("#" + opts[_plotKey].divs.main + " .study-view-drag-icon").css('display', 'block');
    }

    function highlightCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path'),
            _hiddenDotsLength = _hiddenDots.length;
            
        for ( var i = 0; i < _hiddenDotsLength; i++) {
            $(_hiddenDots[i]).css('opacity', '.6');
        }
        $("#" + _curveId + "-line").css('stroke-width', '3px');
    }

    function resetCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path'),
            _hiddenDotsLength = _hiddenDots.length;
        
        for ( var i = 0; i < _hiddenDotsLength; i++) {
            $(_hiddenDots[i]).css('opacity', '0');
        }
        $("#" + _curveId + "-line").css('stroke-width', '');
    }

    function removeCurveFunc(_index, _plotKey) {
        curveInfo[_plotKey].splice(_index, 1);
    }
    
    /* 
     * Generate survival plot division 
     * @param {object} _opt
     */
    function createDiv(_opt) {
        var _div = "<div id='" + _opt.divs.main +
                "' class='study-view-dc-chart w2 h1half study-view-survival-plot'>" +
                "<div id='" + _opt.divs.headerWrapper +
                "' class='study-view-survival-plot-header-wrapper'>" +
                "<chartTitleH4 oValue='" + _opt.title + "' id='" + _opt.divs.title +
                "' class='study-view-survival-plot-title'>" + _opt.title + "</chartTitleH4>" +
                "<div id='" + _opt.divs.header +
                "' class='study-view-survival-plot-header' style='float:right'>" +
                "<img id='"+_opt.divs.downloadIcon+"' class='study-view-download-icon' src='images/in.svg'/>" +
                "<img style='float:left; width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-drag-icon' src='images/move.svg'/>" +
                "<span class='study-view-chart-plot-delete study-view-survival-plot-delete'>x</span>" +
                "</div></div>" +
                "<div id='" + _opt.divs.loader + "' class='study-view-loader'>" +
                "<img src='images/ajax-loader.gif'/></div>" +
                "<div id='" + _opt.divs.body + "' class='study-view-survival-plot-body'>" +
                "<div id='" + _opt.divs.bodySvg + "' style='float:left'></div>" +
                "<div id='" + _opt.divs.bodyLabel +
                "' class='study-view-survival-plot-body-label'></div>" +
                "<div id='" + _opt.divs.pvalMatrix + "' style='text-align:center'></div>"+
                "</div></div>";

        $("#study-view-charts").append(_div);
    }

    /*
     Convert input data into survivalProxy required format
     @param  _plotInfo   the plot basic information
     ---- format----
     { 
     identifier1: {
     name: '',
     property: [''],
     status: [['']],
     caseLists: {
     identifier1: {
     caseIds: [],
     color: ''
     },
     identifier1: 
     }
     },
     identifier1:
     }
     */
    function dataProcess(_plotInfo) {
        var _numOfValuedCase = 0;
        var _plotData = {};

        for (var i = 0; i < oDataLength; i++) {
            if (oData[i].hasOwnProperty(_plotInfo.property[0]) && oData[i].hasOwnProperty(_plotInfo.property[1])) {
                var _time = oData[i][_plotInfo.property[0]],
                        _status = oData[i][_plotInfo.property[1]].toUpperCase(),
                        _caseID = oData[i].CASE_ID;
                _plotData[_caseID] = {};

                _plotData[_caseID].case_id = _caseID;

                if (_plotInfo.status[0].indexOf(_status) !== -1) {
                    _plotData[_caseID].status = '0';
                } else if (_plotInfo.status[1].indexOf(_status) !== -1) {
                    _plotData[_caseID].status = '1';
                } else {
                    _plotData[_caseID].status = 'NA';
                }
                
                if (isNaN(_time)) {
                    _plotData[_caseID].months = 'NA';
                } else {
                    _plotData[_caseID].months = Number(_time);
                }
            }
        }


        //Refind search data, if only one or no case has months information,
        //the survival plot should not be initialized.
        for (var key in _plotData) {
            if (_plotData[key].months !== 'NA') {
                _numOfValuedCase++;
            }
        }

        if (_numOfValuedCase < 2) {
            _plotData = {};
        }
        
        return _plotData;
    }

    /*
     * Initilize all options for current survival plot
     * @param _index The survival plot identifier
     * @return _opts The initilized option object
     */
    function initOpts(_index, _key) {
        var _opts = {};

        _opts.index = _index;
        _opts.key = _key;
        _opts.title = plotsInfo[_key].name;
        _opts.divs = {};
        _opts.divs.main = "study-view-survival-plot-" + _index;
        _opts.divs.title = "study-view-survival-pot-title-" + _index;
        _opts.divs.header = "study-view-survival-plot-header-" + _index;
        _opts.divs.headerWrapper = "study-view-survival-plot-header-wrapper-" + _index;
        _opts.divs.body = "study-view-survival-plot-body-" + _index;
        _opts.divs.bodySvg = "study-view-survival-plot-body-svg-" + _index;
        _opts.divs.bodyLabel = "study-view-survival-plot-body-label-" + _index;
        _opts.divs.pdf = "study-view-survival-plot-pdf-" + _index;
        _opts.divs.pdfName = "study-view-survival-plot-pdf-name-" + _index;
        _opts.divs.pdfValue = "study-view-survival-plot-pdf-value-" + _index;
        _opts.divs.svg = "study-view-survival-plot-svg-" + _index;
        _opts.divs.svgName = "study-view-survival-plot-svg-name-" + _index;
        _opts.divs.svgValue = "study-view-survival-plot-svg-value-" + _index;
        _opts.divs.menu = "study-view-survival-plot-menu-" + _index;
        _opts.divs.loader = "study-view-survival-plot-loader-" + _index;
        _opts.divs.downloadIcon = "study-view-survival-download-icon-" + _index;
        _opts.divs.pvalMatrix = "study-view-survival-pval-matrix-" + _index;
        
        //plot in _opts is for survival plot
        _opts.plot = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);
        _opts.plot.text.xTitle = "Months Survival";
        _opts.plot.text.yTitle = "Surviving";
        _opts.plot.text.qTips.estimation = "Survival estimate";
        _opts.plot.text.qTips.censoredEvent = "Time of last observation";
        _opts.plot.text.qTips.failureEvent = "Time of death";
        _opts.plot.settings.canvas_width = 365;
        _opts.plot.settings.canvas_height = 310;
        _opts.plot.settings.chart_width = 290;
        _opts.plot.settings.chart_height = 250;
        _opts.plot.settings.chart_left = 70;
        _opts.plot.settings.chart_top = 5;
        _opts.plot.settings.include_legend = false;
        _opts.plot.settings.include_pvalue = false;
        _opts.plot.style.axisX_title_pos_x = 200;
        _opts.plot.style.axisX_title_pos_y = 295;
        _opts.plot.style.axisY_title_pos_x = -120;
        _opts.plot.style.axisY_title_pos_y = 20;
        _opts.plot.divs.curveDivId = "study-view-survival-plot-body-svg-" + _index;
        _opts.plot.divs.headerDivId = "";
        _opts.plot.divs.infoTableDivId = "study-view-survival-plot-table-" + _index;
        _opts.plot.text.infoTableTitles.total_cases = "#total cases";
        _opts.plot.text.infoTableTitles.num_of_events_cases = "#cases deceased";
        _opts.plot.text.infoTableTitles.median = "median months survival";

        if(_key === 'DFS') {
            _opts.plot.text.qTips.estimation = "Disease free estimate";
        }
        
        return _opts;
    }

    function redrawView(_plotKey, _casesInfo, _selectedAttr) {
        var _color = "";
        var _casesInfoKeys = Object.keys(_casesInfo);
        var _keysLength = _casesInfoKeys.length;
        var _showedCharts = StudyViewInitCharts.getShowedChartsInfo(),
            _selectedAttrType = _showedCharts['type'][_selectedAttr];
        
        inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        curveInfo[_plotKey] = [];
        
        if(_selectedAttrType === 'bar') {
            _casesInfoKeys.sort(function(a, b) {
                if(a === 'NA') {
                    return 1;
                } else if(b === 'NA') {
                    return -1;
                } else {
                    var _a = a.split('~').map(function(_item) {
                        return Number(_item);
                    });
                    var _b = b.split('~').map(function(_item) {
                        return Number(_item);
                    });

                    if(_a.length < 2 || _b.length < 2) {
                        StudyViewUtil.echoWarningMessg('RedrawView Error:  bar chart key format is wrong.');
                    }
                    
                    if(_a[0] < _b[0]) {
                        return -1;
                    } else if(_a[0] > _b[0]){
                        return 1;
                    } else {
                        if(_a[1] < _b[1]) {
                            return -1;
                        }else {
                            return 1;
                        }
                    }
                }
            });
        }else if (_selectedAttrType === 'pie' || typeof _selectedAttrType === 'undefined') {
            _casesInfoKeys.sort(function(a, b) {
                if(a < b) {
                    return -1;
                } else {
                    return 1;
                }
            });
        }else {
            StudyViewUtil.echoWarningMessg('Unknown chart type in redrawView');
        }
        
        
        for (var i = 0; i < _keysLength; i++) {
            var instanceData = new SurvivalCurveProxy();
            var key = _casesInfoKeys[i];
            instanceData.init(aData[_plotKey], _casesInfo[key].caseIds, kmEstimator, logRankTest);

            //If no data return, will no draw this curve
            if (instanceData.getData().length > 0) {
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                _color = _casesInfo[key].color;

                if (_color) {
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotKey;
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);

                    var _curveInfoDatum = {
                        name: key,
                        color: _color,
                        caseList: _casesInfo[key].color,
                        data: instance
                    };

                    curveInfo[_plotKey].push(_curveInfoDatum);
                } else {
                    //alert("Sorry, you can not create more than 30 curves.");
                    //break;
                }
            }
        }
        
        if(curveInfo[_plotKey].length > 1) {
            createPvalMatrix(_plotKey, curveInfo[_plotKey]);
        }
        
        var inputArrLength = inputArr.length;
        for (var i = 0; i < inputArrLength; i++) {
            survivalPlot[_plotKey].addCurve(inputArr[i]);
        }
        
        addEvents(_plotKey);
    }
    
    /*
     * Initialize survival plot by calling survivalCurve component
     * 
     * @param {object}  _casesInfo  Grouped cases information.
     * @param {object}  _data       The processed data by function dataprocess.
     * @param {object}  _plotIndex  The selected plot indentifier.
     */
    function initView(_casesInfo, _data, _plotKey) {
        var _color = "",
                inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();   

        curveInfo[_plotKey] = [];

        for (var key in _casesInfo) {
            var instanceData = new SurvivalCurveProxy();
            
            instanceData.init(_data, _casesInfo[key].caseIds, kmEstimator, logRankTest);
            //If no data return, will no draw this curve
            if (instanceData.getData().length > 0) {
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                _color = _casesInfo[key].color;
                if (_color) {
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotKey;
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);
                    var _curveInfoDatum = {
                        name: key,
                        color: _color,
                        caseList: _casesInfo[key].caseIds,
                        data: instance
                    };
                    curveInfo[_plotKey].push(_curveInfoDatum);
                } else {
                    alert("Sorry, you can not create more than 30 curves.");
                    break;
                }
            }
        }

        survivalPlot[_plotKey] = new SurvivalCurve();
        survivalPlot[_plotKey].init(inputArr, opts[_plotKey].plot);
    }

    
    /**
     * Redraw curves based on selected cases and unselected cases
     * 
     * @param {type} _casesInfo     the same as _casesInfo in initView
     * @param {type} _selectedAttr  the selected attribute which will be used to
     *                              seperate cases. Can be false or ''.
     */
    function redraw(_casesInfo, _selectedAttr) {
        for (var key in plotsInfo) {
            var _curveInfoLength = curveInfo[key].length;
            for (var i = 0; i < _curveInfoLength; i++) {
                survivalPlot[key].removeCurve(curveInfo[key][i].color.toString().substring(1) + "-" + key);
            }
            
            $("#" + opts[key].divs.main).qtip('destroy', true);
            
            kmEstimator = "";
            logRankTest = "";
            delete curveInfo[key];

            redrawView(key, _casesInfo, _selectedAttr[0]);
            drawLabels(key);
            if (typeof _selectedAttr !== 'undefined') {
                StudyViewUtil.changeTitle("#" + opts[key].divs.main + " chartTitleH4", _selectedAttr[1], false);
            }
            addEvents(key);
        }
    }
    
    /**
     * The main function to draw survival plot labels.
     * 
     * @param {type} _plotKey the current selected plot indentifier.
     */
    function drawLabels(_plotKey) {
        var _svg = '',
            _curveInfo = curveInfo[_plotKey],
            _numOfLabels = _curveInfo.length,
            _width = 0,
            _height = _numOfLabels * 20 - 5;
        
        $("#" + opts[_plotKey].divs.main + " svg").qtip('destroy', true);
       
        if (_numOfLabels === 0) {
            $("#" + opts[_plotKey].divs.bodyLabel).css('display', 'none');
        } else {
            //TODO: this width is calculated by maximum name length multiply
            //a constant, need to be changed later
            for (var i = 0; i < _numOfLabels; i++) {
                if (_curveInfo[i].name.length * 10 > _width) {
                    _width = _curveInfo[i].name.length * 10;
                }
            }

            _width += 30;
            
            $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();

            _svg = d3.select("#" + opts[_plotKey].divs.bodyLabel)
                    .append("svg")
                    .attr('width', _width)
                    .attr("height", _height);

            drawNewLabels(_plotKey, _svg, 0, _width);
        }
    }

    /**
     * Draw basic label componets:  one rect, one lable name, 
     *                              icons(pin or delete icons)
     * 
     * @param {type} _plotKey the current selected plot identifier.
     * @param {type} _svg       the svg where to draw labels.
     * @param {type} _index     the label index in current plot.
     * @param {type} _color     the label color.
     * @param {type} _textName  the label name.
     * @param {type} _iconType  'pin' or 'close', pin will draw pin icon and
     *                          delete icon, close will only draw delete icon.
     * @param {type} _svgWidth  the svg width.
     */
    function drawLabelBasicComponent(_plotKey, _svg, _index, _color, _textName, _iconType, _svgWidth) {
        var _g = _svg.append("g").attr('transform', 'translate(0, ' + (_index * 20) + ')');
       
        _g.append("rect")
                .attr('width', 10)
                .attr('height', 10)
                .attr('fill', _color);

        _g.append("text")
                .attr('x', 15)
                .attr('y', 10)
                .attr('fill', 'black')
                .attr('font', '12px')
                .attr('id', 'survival_label_text_' + _plotKey + "_" + _index)
                .attr('oValue', _textName)
                .text(_textName);

        if (_iconType === 'pin') {

            var _image = _g.append("image")
                    .attr('x', _svgWidth - 15)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'close');

        } else if (_iconType === 'close') {
            var _image = _g.append("image")
                    .attr('x', _svgWidth - 15)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'saved-close');
        } else {
            //TODO:
        }
    }
    
    /**
     * Calling drawLabelBasicComponent to draw all new labels including 
     * curve color, name and icontype = 'pin'.
     * 
     * @param {type} _plotKey     the selected plot identifier.
     * @param {type} _svg           the svg where to draw labels.
     * @param {type} _startedIndex  
     * @param {type} _svgWidth      the svg width.
     */
    function drawNewLabels(_plotKey, _svg, _startedIndex, _svgWidth) {
        var _numOfLabels = curveInfo[_plotKey].length;
        for (var i = 0; i < _numOfLabels; i++) {
            drawLabelBasicComponent(
                    _plotKey, 
                    _svg, 
                    i + _startedIndex, 
                    curveInfo[_plotKey][i].color, 
                    curveInfo[_plotKey][i].name, 
                    'pin', 
                    _svgWidth);
        }
    }

    
    /**
     * Will be called when user pin/delete labeles
     * @param {type} _plotKey
     */
    function redrawLabel(_plotKey) {
        $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();
        drawLabels(_plotKey);
        addEvents(_plotKey);
    }
    
    /**
     * 
     * @param {type} _plotsInfo
     * @param {type} _data      all data before prcessing, and clone it to oData.
     */
    function createCurves(_plotsInfo, _data) {
        var _keys = Object.keys(_plotsInfo);
        numOfPlots = Object.keys(_plotsInfo).length;
        plotsInfo = _plotsInfo;
        oData = _data;
        oDataLength = _data.length;

        for (var i = 0; i < numOfPlots; i++) {
            plotBasicFuncs(i, _keys[i]);
        }

        //The initStatus will be used from other view
        initStatus = true;
    }

    function plotBasicFuncs(_index, _key) {
        aData[_key] = {};
        opts[_key] = {};
        aData[_key] = dataProcess(plotsInfo[_key]);

/*
        for(var _key in aData[_index]){
            console.log("-----");
            console.log(_key);
            console.log(aData[_index][_key].months);
            console.log(aData[_index][_key].status);
            console.log();
        }
        */
        //If no data returned, this survival plot should not be initialized.
        if (Object.keys(aData[_key]).length !== 0) {
            opts[_key] = initOpts(_index, _key);
            createDiv(opts[_key]);
            initView(plotsInfo[_key].caseLists, aData[_key], _key);
            drawLabels(_key);
            addEvents(_key);
        } else {
            console.log("No data for Survival Plot: " + _key);
        }
    }

    function getNumOfPlots() {
        return numOfPlots;
    }

    function detectLabelPosition() {
        for (var i = 0; i < numOfPlots; i++) {
            if ($("#" + opts[i].divs.bodyLabel).css('display') === 'block') {
                StudyViewUtil.changePosition(
                        '#' + opts[i].divs.main,
                        '#' + opts[i].divs.bodyLabel,
                        "#dc-plots");
            }
        }
    }

    return {
        init: function(_plotsInfo, _data) {
            createCurves(_plotsInfo, _data);
        },
        getInitStatus: getInitStatus,
        redraw: redraw,
        redrawLabel: redrawLabel,
        getNumOfPlots: getNumOfPlots,
        detectLabelPosition: detectLabelPosition
    };
})();