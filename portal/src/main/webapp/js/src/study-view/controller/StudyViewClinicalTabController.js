
/*
 * This class is designed to control the logic for Clinial Tab in Study View
 * 
 * @autor Hongxin Zhang
 * 
 */


var StudyViewClinicalTabController = (function(){
    function init(_data){
        StudyViewInitClinicalTab.init('clinical_table', _data);
    }
    
    return {
        init: init
    };
})();