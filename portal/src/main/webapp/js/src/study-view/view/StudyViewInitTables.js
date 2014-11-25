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
        if(typeof callback === 'function'){
            callback();
        }
    }
    
    function initData(input) {
        var attr = input.data.attr,
            arr = input.data.arr,
            numOfCases = input.numOfCases;
        
        attr.forEach(function(e, i) {
            var _datum = arr[e.name],
                _worker = {};
            
            _worker.opts = {};
            _worker.data = {};
            
            switch (e.name) {
                case 'mutatedGenes':
                    _worker.opts.title = 'Mutated Genes';
                    _worker.data.attr = [{
                            name: 'gene',
                            displayName: 'Gene'
                        },{
                            name: 'numOfMutations',
                            displayName: '# Mutations'
                        },{
                            name: 'mutatedSamples',
                            displayName: 'Mutated Samples'
                        },{
                            name: 'sampleRate',
                            displayName: 'Smaple Mutated Frequency'
                        } 
                    ];
                    _worker.data.arr = mutatedGenesData(_datum, numOfCases);
                    break;
                case 'cna':
                console.log(_datum);
                    _worker.opts.title = 'CNA';
                    _worker.data.attr = [{
                            name: 'gene',
                            displayName: 'Gene'
                        },{
                            name: 'ampDel',
                            displayName: 'AMP/DEL'
                        },{
                            name: 'count',
                            displayName: 'Count'
                        }
                        
//                            name: 'mutatedSamples',
//                            displayName: 'Mutated Samples'
//                        },{
//                            name: 'sampleRate',
//                            displayName: 'Smaple Mutated Frequency'
//                        } 
                    ];
                    _worker.data.arr = cnaData(_datum, numOfCases);
                    break;
                default:
                    _worker.opts.title = 'Unknown';
                    break;
            }
            _worker.opts.name = e.name;
            _worker.opts.tableId = 'study-view-table-' + e.name;
            _worker.opts.parentId = 'study-view-charts';
            _worker.opts.webService = e.webService;
            workers.push(_worker);
        });
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
            var datum = {},
                caseIds = data[i].caseIds.split(',');
            
            datum.gene = data[i].gene_symbol;
            datum.numOfMutations = Number(data[i].num_muts);
            datum.mutatedSamples = caseIds.filter(function(elem, pos) {
                return caseIds.indexOf(elem) === pos;
            }).length;
            datum.sampleRate = 
                    (datum.mutatedSamples / Number(numOfCases)* 100).toFixed(1) + '%';
            genes.push(datum);
        }
        return genes;
    }
    
    function cnaData(data, numOfCases) {
        var genePair = {},
            genes = [];
        
        for(var i = 0, dataL = data.length; i < dataL; i++){
            var _genes = data[i].nonSangerGenes.concat(data[i].sangerGenes);
            
            _genes.forEach(function(e){
                var _key = e + '#*#*#' + (data[i].ampdel?'amp':'del');
                if(genePair.hasOwnProperty(_key)) {
                    genePair[_key]++;
                }else {
                    genePair[_key] = 1;
                }
            });
        }
        for(var key in genePair) {
            var _pair = key.split('#*#*#'),
                _gene = _pair[0],
                _ampDel = _pair[1];
        
            genes.push({
                gene: _gene,
                ampDel: _ampDel,
                count: genePair[key]
            });
        }
        return genes;
    }
    
    function redraw(data){
        var selectedCasesL = data.selectedCases.length;
        //Start loaders
        workers.forEach(function(e, i){
            e.tableInstance.startLoading();
        });
        
        workers.forEach(function(e, i){
            if(selectedCasesL !== 0){
                $.ajax(data.webService[e.opts.name])
                    .done(function(d){
                        switch (e.opts.name) {
                            case 'mutatedGenes':
                                workers[i].data.arr = mutatedGenesData(d, selectedCasesL);
                                break;
                            case 'cna':
                                workers[i].data.arr = cnaData(d, selectedCasesL);
                                break;
                            default:
                                break;
                        }
                        
                        e.tableInstance.redraw(workers[i].data, function(){
                            e.tableInstance.stopLoading();
                        });
                    });

            }else{
                workers[i].data.arr = [];
                e.tableInstance.redraw(workers[i].data);;
            }
        });
    }
    
    return {
        init: init,
        redraw: redraw,
        getInitStatus: function() {
            if(workers.length > 0) {
                return true;
            }else {
                return false;
            }
        },
        resizeTable: function() {
            workers.forEach(function(e, i) {
                e.tableInstance.resize();
            });
        }
    };
    
})();