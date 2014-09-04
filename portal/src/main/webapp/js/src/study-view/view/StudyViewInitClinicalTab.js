

var StudyViewInitClinicalTab = (function(){
    
    var dataTable;
    
    
    function init(_tableID, _tableContainerId, _data){
//        tableID = _tableID;
//        aaData = _aaData;
//        aoColumns = _aoColumns;
        dataTable = new DataTable();
        dataTable.init(_tableID, _tableContainerId, _data);
        
        //initDataTable();
    }
    
    return {
        init: init,
        getDataTable: function() {
            return dataTable;
        }
    };
})();