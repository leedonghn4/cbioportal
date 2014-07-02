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

/* 
 * Data Manager for the one-gene plots sub tab under the "Plots" tab
 *
 * @author: YichaoS <yichao@cbio.mskcc.org>
 * @date: Jun 2014
 *
 */

var OneGeneDataProxy = (function() {

        //Mappings
        var gistic_txt_val = {
                "-2": "Homdel",
                "-1": "Hetloss",
                "0": "Diploid",
                "1": "Gain",
                "2": "Amp"
            },
            mutations_alias = { //mapping to the keys in mutation style objects in oneGene.js in view/
                frameshift : "frameshift",
                in_frame : "in_frame",
                missense : "missense",
                nonsense : "nonsense",
                splice : "splice",
                nonstop : "nonstop",
                nonstart : "nonstart",
                non : "non"
            };
        //Options
        var userSelection = { 
	        	gene: "",
	        	plot_type: "",
	        	data_type: {
	        		"x" : {},
	        		"y" : {}
	    		}        
	        }; 
	    //Data Container
        var sampleSetLength = 0,
            dotsGroup = [],
            singleDot = {
                caseId : "",
                xVal : "",
                yVal : "",
                mutationDetail : "",  //Mutation ID
                mutationType : "",
                //gisticType : "" //Discretized(GISTIC/RAE) Annotation
            },  
            status = {
                xHasData: false,
                yHasData: false,
                combineHasData: false
            },
            attr = { //attributes of data set
                min_x: 0,
                max_x: 0,
                min_y: 0,
                max_y: 0,
                pearson: 0,
                spearman: 0
            };

        function fetchPlotsData(profileDataResult) {
            var resultObj = profileDataResult[userSelection.gene];
            for (var key in resultObj) {  //key is sample id
                sampleSetLength += 1;
                var _obj = resultObj[key];
                var _singleDot = jQuery.extend(true, {}, singleDot);
                //extract x, y axis value 
                _singleDot.caseId = key;
                _singleDot.xVal = _obj[userSelection.data_type.x.id];
                _singleDot.yVal = _obj[userSelection.data_type.y.id];
                //Set Data Status
                if (!oneGeneUtil.isEmpty(_singleDot.xVal)) {
                    status.xHasData = true;
                }
                if (!oneGeneUtil.isEmpty(_singleDot.yVal)) {
                    status.yHasData = true;
                }
                //Push non-empty data points into the dots array
                if (!oneGeneUtil.isEmpty(_singleDot.xVal) &&
                    !oneGeneUtil.isEmpty(_singleDot.yVal) &&
                    !oneGeneUtil.isEmpty(_singleDot.gisticType)) {
                    dotsGroup.push(_singleDot);
                    status.combineHasData = true;
                }
            }
        }

        function translateMutationType(mutationTypeResult) {
            //Map mutation type for each individual cases
            var mutationDetailsUtil =
                new MutationDetailsUtil(new MutationCollection(mutationTypeResult));
            var mutationMap = mutationDetailsUtil.getMutationCaseMap();
            $.each(dotsGroup, function(index, dot) {
                if (!mutationMap.hasOwnProperty(dot.caseId.toLowerCase())) {
                    dot.mutationType = mutations_alias.non;
                } else {
                    var _mutationTypes = [], //one case can have multi-mutations
                        _proteinChangeStr = "";
                    $.each(mutationMap[dot.caseId.toLowerCase()], function (index, val) {
                        //Map mutation type
                        if ((val.mutationType === "Frame_Shift_Del")||(val.mutationType === "Frame_Shift_Ins")) {
                            _mutationTypes.push(mutations_alias.frameshift);
                        } else if ((val.mutationType === "In_Frame_Del")||(val.mutationType === "In_Frame_Ins")) {
                            _mutationTypes.push(mutations_alias.in_frame);
                        } else if ((val.mutationType === "Missense_Mutation")||(val.mutationType === "Missense")) {
                            _mutationTypes.push(mutations_alias.missense);
                        } else if ((val.mutationType === "Nonsense_Mutation")||(val.mutationType === "Nonsense")) {
                            _mutationTypes.push(mutations_alias.nonsense);
                        } else if ((val.mutationType === "Splice_Site")||(val.mutationType === "Splice_Site_SNP")) {
                            _mutationTypes.push(mutations_alias.splice);
                        } else if (val.mutationType === "NonStop_Mutation") {
                            _mutationTypes.push(mutations_alias.nonstop);
                        } else if (val.mutationType === "Translation_Start_Site") {
                            _mutationTypes.push(mutations_alias.nonstart);
                        } else { //Fusion etc. new mutation types
                            _mutationTypes.push(mutations_alias.other);
                        }
                        //Simply append protein change
                        if (_proteinChangeStr !== "") {
                            _proteinChangeStr += ", " + val.proteinChange;
                        } else {
                            _proteinChangeStr += val.proteinChange;
                        }
                    });
                    //Re-order mutations in one case based on a priority list
                    var mutationPriorityList = []; //define the priority list
                    mutationPriorityList[mutations_alias.frameshift] = "0";
                    mutationPriorityList[mutations_alias.in_frame] = "1";
                    mutationPriorityList[mutations_alias.missense] = "2";
                    mutationPriorityList[mutations_alias.nonsense] = "3";
                    mutationPriorityList[mutations_alias.splice] = "4";
                    mutationPriorityList[mutations_alias.nonstop] = "5";
                    mutationPriorityList[mutations_alias.nonstart] = "6";
                    mutationPriorityList[mutations_alias.other] = "7"
                    mutationPriorityList[mutations_alias.non] = "8";
                    var _primaryMutation = _mutationTypes[0];
                    $.each(_mutationTypes, function(index, val) {
                        if (mutationPriorityList[_primaryMutation] > mutationPriorityList[val]) {
                            _primaryMutation = val;
                        }
                    });
                    dot.mutationType = _primaryMutation;
                    dot.mutationDetail = _proteinChangeStr;
                }
            });
        }

        function prioritizeMutatedCases() {
            var nonMutatedData = [];
            var mutatedData= [];
            var dataBuffer = [];
            dotsGroup.forEach (function(entry) {
                if (!oneGeneUtil.isEmpty(entry.mutationDetail)) {
                    mutatedData.push(entry);
                } else {
                    nonMutatedData.push(entry);
                }
            });
            nonMutatedData.forEach (function(entry) {
                dataBuffer.push(entry);
            });
            mutatedData.forEach (function(entry) { //mutated data points got plotted last
                dataBuffer.push(entry);
            });
            dotsGroup = dataBuffer;
        }

        function analyseData() {
            var tmp_xData = [];
            var tmp_xIndex = 0;
            var tmp_yData = [];
            var tmp_yIndex = 0;
            for (var j = 0; j < dotsGroup.length; j++){
                if (!oneGeneUtil.isEmpty(dotsGroup[j].xVal) &&
                    !oneGeneUtil.isEmpty(dotsGroup[j].yVal)) {
                    tmp_xData[tmp_xIndex] = dotsGroup[j].xVal;
                    tmp_xIndex += 1;
                    tmp_yData[tmp_yIndex] = dotsGroup[j].yVal;
                    tmp_yIndex += 1;
                }
            }
            attr.min_x = Math.min.apply(Math, tmp_xData);
            attr.max_x = Math.max.apply(Math, tmp_xData);
            attr.min_y = Math.min.apply(Math, tmp_yData);
            attr.max_y = Math.max.apply(Math, tmp_yData);

            //Calculate the co-express/correlation scores
            //(When data is discretized)
            if (!oneGeneUtil.plotsIsDiscretized(userSelection.data_type)) {
                var tmpGeneXcoExpStr = "",
                    tmpGeneYcoExpStr = "";
                $.each(dotsGroup, function(index, obj) {
                    tmpGeneXcoExpStr += obj.xVal + " ";
                    tmpGeneYcoExpStr += obj.yVal + " ";
                });
                var paramsCalcCoexp = {
                    gene_x : tmpGeneXcoExpStr,
                    gene_y : tmpGeneYcoExpStr
                };
                $.post("calcCoExp.do", paramsCalcCoexp, getCalcCoExpCallBack, "json");
            } else {
                //$('#view_title').show();
                //$('#plots_box').show();
                //$('#loading-image').hide();
                //View.init();                
            }
        }

        function getCalcCoExpCallBack(result) {
        }

        function getDataCallback(profileData, mutationData) { //convert/assemble the raw data
            fetchPlotsData(profileData);
            translateMutationType(mutationData);
            prioritizeMutatedCases();
            analyseData();
        }

        function getData() {
        	var _profileIdStr = userSelection.data_type.x.id + " " +
        						userSelection.data_type.y.id + " ";
            PlotsTabDataProxy.getData(
                window.PortalGlobals.getCancerStudyId(),
                userSelection.gene,
                _profileIdStr,
                window.PortalGlobals.getCaseSetId(),
                window.PortalGlobals.getCaseIdsKey(),
                getDataCallback
            );
        }

        function getUserSelection() {
		    userSelection.gene = document.getElementById("one_gene_gene_list").value;
		    userSelection.plot_type = document.getElementById("one_gene_plot_type").value;
		    var _dataTypeUserSelectionArr = [];
		    $("#one_gene_data_type_div").find("select").each(function() {
		    	var _obj = {};
		    	_obj.id = $(this).val().split("|")[0];
		    	_obj.description = $(this).val().split("|")[1];
		    	_obj.text = $(this).find('option:selected').text();
		    	_dataTypeUserSelectionArr.push(_obj);
		    });
		    userSelection.data_type.x = _dataTypeUserSelectionArr[0];
		    userSelection.data_type.y = _dataTypeUserSelectionArr[1];
        }

        return {
        	init: function(callback_func) {
        		getUserSelection();
        		getData(); //profile data 
		        // var sel = document.getElementById("data_type_copy_no");
		        // var vals = [];
		        // for (var i = 0; i < sel.children.length; ++i) {
		        //     var child = sel.children[i];
		        //     if (child.tagName == 'OPTION') vals.push(child.value.split("|")[0]);
		        // }
		        // if (vals.indexOf(cancer_study_id + "_gistic") !== -1) {
		        //     discretizedDataTypeIndicator = cancer_study_id + "_gistic";
		        // } else if (vals.indexOf(cancer_study_id + "_cna") !== -1) {
		        //     discretizedDataTypeIndicator = cancer_study_id + "_cna";
		        // } else if (vals.indexOf(cancer_study_id + "_CNA") !== -1) {
		        //     discretizedDataTypeIndicator = cancer_study_id + "_CNA";
		        // } else if (vals.indexOf(cancer_study_id + "_cna_rae") !== -1) {
		        //     discretizedDataTypeIndicator = cancer_study_id + "_cna_rae";
		        // }
		        // var _profileIdsStr = cancer_study_id + "_mutations" + " " +
		        //     discretizedDataTypeIndicator + " " +
		        //     userSelection.copy_no_type + " " +
		        //     userSelection.mrna_type + " " +
		        //     userSelection.rppa_type + " " +
		        //     userSelection.dna_methylation_type;
		        // Plots.getProfileData(
		        //     userSelection.gene,
		        //     _profileIdsStr,
		        //     patient_set_id,
		        //     patient_ids_key,
		        //     getProfileDataCallBack
        		// );        		
        	},
            callback: function(profileDataResult, mutationTypeResult) {
                status.xHasData = false;
                status.yHasData = false;
                status.combineHasData = false;
                caseSetLength = 0;
                dotsGroup.length = 0;
                fetchPlotsData(profileDataResult);
                if (mutationTypeResult !== "") {
                    translateMutationType(mutationTypeResult);
                    prioritizeMutatedCases();
                }
                analyseData();
            },
            getDotsGroup: function() { return dotsGroup; },
            getDataStatus: function() { return status; },
            getDataAttr: function() { return attr; }
        };

    }());