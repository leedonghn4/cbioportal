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

 var PlotsTabDataProxy = (function() {

 	//User input
 	var input_cancerStyId = "", 
 		input_geneIds = "",
 		input_profileIds = "",
 		input_sampleSetId = "",
 		input_sampleIdsKey = "",
 		_callbackFunc = "";

 	//Tmp data container
 	var _profileDataResult = {},
 		_mutationResult = {};

 	function convertRawData() {
 		_callbackFunc(_profileDataResult, _mutationResult);
 	}

 	function getMutationDataCallBack(result) {
 		_mutationResult = result;
 		convertRawData();
 	}

 	function getMutationData(result) {
 		_profileDataResult = result;
        var proxy = DataProxyFactory.getDefaultMutationDataProxy();
        proxy.getMutationData(input_geneIds, getMutationDataCallBack);
 	}

 	function getProfileData() {
 		var _params = {
 			cancer_study_id: input_cancerStyId,
 			gene_list: input_geneIds,
 			genetic_profile_id: input_profileIds,
 			case_set_id: input_sampleSetId,
 			case_ids_key: input_sampleIdsKey
 		};
 		$.post("getProfileData.json", _params, getMutationData, "json");
 	}

 	function getData(_cancerStudyId, _geneIds, _profileIds, _sampleSetId, _sampleIdsKey, callback_func) {
 		input_cancerStyId = _cancerStudyId;
 		input_geneIds = _geneIds;
 		input_profileIds = _profileIds;
 		input_sampleSetId = _sampleSetId;
 		input_sampleIdsKey = _sampleIdsKey;
 		_callbackFunc = callback_func;
 		getProfileData();
 	}

 	return {
 		getData: getData
 	}

 }());