/**
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
 * 
 * @author Hongxin ZHang
 * @date Nov. 2014
 * 
 */


var StudyViewInitTables = (function() {
    var workers = [];
    
    function init(input,callback) {
        initData(input);
        initTables();
        callback();
    }
    
    function initData(input) {
        var data = input.data,
            numOfCases = input.numOfCases;
    
        for(var key in data) {
            var _datum = data[key],
                _worker = {};
            
            _worker.opts = {};
            _worker.data = {};
            
            switch (key) {
                case 'mutatedGenes':
                    _worker.opts.title = 'Mutated Genes';
                    _worker.data = {};
                    _worker.data.attr = [{
                            name: 'name',
                            displayName: 'Gene'
                        },{
                            name: 'numOfMutations',
                            displayName: 'Mutated Samples'
                        },{
                            name: 'sampleRate',
                            displayName: 'Mutated Frequency'
                        }
                    ];
                    _worker.data.arr = mutatedGenesData(_datum, numOfCases);
                case 'cna':
                    _worker.opts.title = 'CNA';
                default:
                    _worker.opts.title = 'Unknown';
            }
            _worker.opts.name = key;
            _worker.opts.tableId = 'study-view-table' + key;
            _worker.opts.parentId = 'study-view-charts';
            workers.push(_worker);
        }
    }
    
    function initTables() {
        workers.forEach(function(e, i){
            workers[i].tableInstance = new Table();
            workers[i].tableInstance.init(e);
        });
    }
    
    function mutatedGenesData(data, numOfCases) {
        var genes = [];
        
        for(var i = 0, dataL = data.length; i < dataL; i++){
            var datum = {};
            
            datum.name = data[i].gene_symbol;
            datum.numOfMutations = Number(data[i].num_muts);
            datum.sampleRate = 
                    (Number(data[i].num_muts) / Number(numOfCases)* 100).toFixed(1) + '%';
            genes.push(datum);
        }
        return genes;
    }
    
    function cnaData() {
        
    }
    
    function redrawWordCloud(){
        var _selectedCases = getSelectedCases(),
        _selectedCasesLength = _selectedCases.length,
        _selectedGeneMutatedInfo = [],
        _filteredMutatedGenes = {},
        _selectedCasesIds = [];
        
        if(_selectedCasesLength !== 0){
            for( var i = 0; i < _selectedCasesLength; i++){
                _selectedCasesIds.push(_selectedCases[i].CASE_ID);
            }

            var mutatedGenesObject = {
                cmd: 'get_smg',
                case_list: _selectedCasesIds.join(' '),
                mutation_profile: StudyViewParams.params.mutationProfileId
            };

            $.when($.ajax({type: "POST", url: "mutations.json", data: mutatedGenesObject}))
            .done(function(a1){
                var i, dataLength = a1.length;

                for( i = 0; i < dataLength; i++){
                    _selectedGeneMutatedInfo.push(a1[i]);
                }

                _filteredMutatedGenes = wordCloudDataProcess(_selectedGeneMutatedInfo, _selectedCasesLength);
                StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
                callBackFunctions();
                $("#study-view-word-cloud-loader").css('display', 'none');
                $("#study-view-word-cloud").css('opacity', '1');
            });
        }else{
            _filteredMutatedGenes = wordCloudDataProcess([], 1);
            StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
            $("#study-view-word-cloud-loader").css('display', 'none');
            $("#study-view-word-cloud").css('opacity', '1');
        }     
    }
    
    return {
        init: init
    };
    
})();