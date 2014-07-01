var PlotsTabMenuDataProxy = (function() {

    var genetic_profile = {
        genetic_profile_mutations : [],
        genetic_profile_mrna : [],
        genetic_profile_copy_no : [],
        genetic_profile_rppa : [],
        genetic_profile_dna_methylation : []
    },
    genetic_profiles = {},
    clinical_attributes = {};

    function getGeneticProfileCallback(result) {
	    for (var gene in result) {
	        var _obj = result[gene];
	        var _genetic_profile = jQuery.extend(true, {}, genetic_profile);
	        for (var key in _obj) {
	            var obj = _obj[key];
	            var profile_type = obj.GENETIC_ALTERATION_TYPE;
	            if (profile_type === "MUTATION_EXTENDED") {
	                _genetic_profile.genetic_profile_mutations.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
	            } else if(profile_type === "COPY_NUMBER_ALTERATION") {
	                _genetic_profile.genetic_profile_copy_no.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
	            } else if(profile_type === "MRNA_EXPRESSION") {
	                _genetic_profile.genetic_profile_mrna.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
	            } else if(profile_type === "METHYLATION") {
	                _genetic_profile.genetic_profile_dna_methylation.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
	            } else if(profile_type === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
	                _genetic_profile.genetic_profile_rppa.push([obj.STABLE_ID, obj.NAME, obj.DESCRIPTION]);
	            }
	        }
	        genetic_profiles[gene] = _genetic_profile;
	    }
	    //Get available clinical attributes for selected study
	    var paramsGetClinicalAttributes = {
	        cmd : "getClinicalData",
	        case_set_id : patient_set_id,
	        format : "json",
	        result_type : "meta"
	    };
	    $.post("webservice.do", paramsGetClinicalAttributes, getClinicalAttrCallBack, "json");
	}

    function getClinicalAttrCallBack(result) {
        clinical_attributes = result;
        PlotsTabView.viewInitCallback();
    }

    return {
        init: function() {
            var paramsGetProfiles = {
                cancer_study_id: cancer_study_id,
                case_set_id: patient_set_id,
                case_ids_key: patient_ids_key,
                gene_list: gene_list_str
            };
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
        },
        getGeneticProfiles: function(selectedGene) {
            return genetic_profiles[selectedGene];
        },
        getClinicalAttributes: function() {
            return clinical_attributes;
        }
    }


}());