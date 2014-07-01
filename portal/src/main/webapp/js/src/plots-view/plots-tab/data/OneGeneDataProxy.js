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
        var caseSetLength = 0,
            dotsGroup = [],
            singleDot = {
                caseId : "",
                xVal : "",
                yVal : "",
                mutationDetail : "",  //Mutation ID
                mutationType : "",
                gisticType : "" //Discretized(GISTIC/RAE) Annotation
            },   //Template for single dot
            status = {
                xHasData: false,
                yHasData: false,
                combineHasData: false
            },
            attr = {
                min_x: 0,
                max_x: 0,
                min_y: 0,
                max_y: 0,
                pearson: 0,
                spearman: 0
            };


        function fetchPlotsData(profileDataResult) {
            var resultObj = profileDataResult[userSelection.gene];
            for (var key in resultObj) {  //key is case id
                caseSetLength += 1;
                var _obj = resultObj[key];
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.caseId = key;
                //TODO: remove hard-coded menu content
                if (OneGeneUtil.plotsTypeIsCopyNo()) {
                    _singleDot.xVal = _obj[userSelection.copy_no_type];
                    _singleDot.yVal = _obj[userSelection.mrna_type];
                } else if (OneGeneUtil.plotsTypeIsMethylation()) {
                    _singleDot.xVal = _obj[userSelection.dna_methylation_type];
                    _singleDot.yVal = _obj[userSelection.mrna_type];
                } else if (OneGeneUtil.plotsTypeIsRPPA()) {
                    _singleDot.xVal = _obj[userSelection.mrna_type];
                    _singleDot.yVal = _obj[userSelection.rppa_type];
                }
                if (_obj.hasOwnProperty(cancer_study_id + "_mutations")) {
                    _singleDot.mutationDetail = _obj[cancer_study_id + "_mutations"];
                    _singleDot.mutationType = _obj[cancer_study_id + "_mutations"]; //Translate into type later
                } else {
                    _singleDot.mutationType = "non";
                }
                if (!OneGeneUtil.isEmpty(_obj[discretizedDataTypeIndicator])) {
                    _singleDot.gisticType = text.gistic_txt_val[_obj[discretizedDataTypeIndicator]];
                } else {
                    _singleDot.gisticType = "NaN";
                }
                //Set Data Status
                if (!OneGeneUtil.isEmpty(_singleDot.xVal)) {
                    status.xHasData = true;
                }
                if (!OneGeneUtil.isEmpty(_singleDot.yVal)) {
                    status.yHasData = true;
                }
                //Push into the dots array
                if (!OneGeneUtil.isEmpty(_singleDot.xVal) &&
                    !OneGeneUtil.isEmpty(_singleDot.yVal) &&
                    !OneGeneUtil.isEmpty(_singleDot.gisticType)) {
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
                    dot.mutationType = mutationStyle.non.typeName;
                } else {
                    var _mutationTypes = []; //one case can have multi-mutations
                    $.each(mutationMap[dot.caseId.toLowerCase()], function (index, val) {
                        if ((val.mutationType === "Frame_Shift_Del")||(val.mutationType === "Frame_Shift_Ins")) {
                            _mutationTypes.push(mutationStyle.frameshift.typeName);
                        } else if ((val.mutationType === "In_Frame_Del")||(val.mutationType === "In_Frame_Ins")) {
                            _mutationTypes.push(mutationStyle.in_frame.typeName);
                        } else if ((val.mutationType === "Missense_Mutation")||(val.mutationType === "Missense")) {
                            _mutationTypes.push(mutationStyle.missense.typeName);
                        } else if ((val.mutationType === "Nonsense_Mutation")||(val.mutationType === "Nonsense")) {
                            _mutationTypes.push(mutationStyle.nonsense.typeName);
                        } else if ((val.mutationType === "Splice_Site")||(val.mutationType === "Splice_Site_SNP")) {
                            _mutationTypes.push(mutationStyle.splice.typeName);
                        } else if (val.mutationType === "NonStop_Mutation") {
                            _mutationTypes.push(mutationStyle.nonstop.typeName);
                        } else if (val.mutationType === "Translation_Start_Site") {
                            _mutationTypes.push(mutationStyle.nonstart.typeName);
                        } else { //Fusion etc. new mutation types
                            _mutationTypes.push(mutationStyle.other.typeName);
                        }
                    });
                    //Re-order mutations in one case based on priority list
                    var mutationPriorityList = [];
                    mutationPriorityList[mutationStyle.frameshift.typeName] = "0";
                    mutationPriorityList[mutationStyle.in_frame.typeName] = "1";
                    mutationPriorityList[mutationStyle.missense.typeName] = "2";
                    mutationPriorityList[mutationStyle.nonsense.typeName] = "3";
                    mutationPriorityList[mutationStyle.splice.typeName] = "4";
                    mutationPriorityList[mutationStyle.nonstop.typeName] = "5";
                    mutationPriorityList[mutationStyle.nonstart.typeName] = "6";
                    mutationPriorityList[mutationStyle.other.typeName] = "7"
                    mutationPriorityList[mutationStyle.non.typeName] = "8";
                    var _primaryMutation = _mutationTypes[0];
                    $.each(_mutationTypes, function(index, val) {
                        if (mutationPriorityList[_primaryMutation] > mutationPriorityList[val]) {
                            _primaryMutation = val;
                        }
                    });
                    dot.mutationType = _primaryMutation;
                }
            });
        }

        function prioritizeMutatedCases() {
            var nonMutatedData = [];
            var mutatedData= [];
            var dataBuffer = [];
            dotsGroup.forEach (function(entry) {
                if (!OneGeneUtil.isEmpty(entry.mutationDetail)) {
                    mutatedData.push(entry);
                } else {
                    nonMutatedData.push(entry);
                }
            });
            nonMutatedData.forEach (function(entry) {
                dataBuffer.push(entry);
            });
            mutatedData.forEach (function(entry) {
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
                if (!OneGeneUtil.isEmpty(dotsGroup[j].xVal) &&
                    !OneGeneUtil.isEmpty(dotsGroup[j].yVal)) {
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
            if (!OneGeneUtil.plotsIsDiscretized()) {
                var tmpGeneXcoExpStr = "",
                    tmpGeneYcoExpStr = "";
                $.each(PlotsData.getDotsGroup(), function(index, obj) {
                    tmpGeneXcoExpStr += obj.xVal + " ";
                    tmpGeneYcoExpStr += obj.yVal + " ";
                });
                var paramsCalcCoexp = {
                    gene_x : tmpGeneXcoExpStr,
                    gene_y : tmpGeneYcoExpStr
                };
                $.post("calcCoExp.do", paramsCalcCoexp, getCalcCoExpCallBack, "json");
            } else {
                $('#view_title').show();
                $('#plots_box').show();
                $('#loading-image').hide();
                View.init();                
            }
        }

        function getDataCallback(result) {
        }

        function getData() {
        	var _profileIdStr = userSelection.data_type.x.id + " " +
        						userSelection.data_type.y.id;
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
		    userSelection.data_type.length = 0;
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