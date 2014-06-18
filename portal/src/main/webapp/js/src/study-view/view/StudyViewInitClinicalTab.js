

var StudyViewInitClinicalTab = (function(){
    
    var dataTable;
    
    
    function init(_tableID, _data){
        dataTable = new DataTable();
        dataTable.init(_tableID, _data);
    }
    
    return {
        init: init,
        getDataTable: function() {
            return dataTable;
        }
    };
})();