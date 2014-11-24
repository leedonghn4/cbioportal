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

var Table = function() {
    var divs = {},
        dataTable = '',
        initStatus = false;
    
    function init(input,callback) {
        console.log(input);
        initData(input.data, input.opts);
        initDiv();
        initTable(input.data);
        initDataTable();
        initStatus = true;
        if(typeof callback !== 'undefined' && callback) {
            callback();
        }
    }
    
    function initData(data, opts) {
        if(typeof opts === 'object' && opts.hasOwnProperty('tableId')) {
           var tableId = opts.tableId;
           
           divs.attachedId = opts.parentId;
           divs.mainId = tableId + '-main';
           divs.titleId = tableId + '-title';
           divs.titleWrapperId = divs.titleId + '-wrapper';
           divs.tableId = tableId;
           divs.headerId = tableId + '-header';
           divs.reloadId = tableId + '-reload-icon';
           divs.downloadId = tableId + '-download-icon';
           divs.downloadWrapperId = tableId + '-download-icon-wrapper'
           divs.loaderId = tableId + '-header';
       }
    }
    
    function initDiv() {
        var _div = "<div id='"+divs.mainId+"' class='study-view-dc-chart h1half w2'>"+
            "<div id='"+divs.titleWrapperId+"'style='height: 16px; width:100%; float:left; text-align:center;'>"+
                "<div style='height:16px;float:right;' id='"+divs.titleWrapperId+"'>"+
                    "<img id='"+divs.reloadId+"' class='study-view-title-icon hidden hover' src='images/reload-alt.svg'/>"+    
                    "<div id='"+divs.downloadWrapperId+"' class='study-view-download-icon'>" +
                        "<img id='"+divs.downloadId+"' style='float:left' src='images/in.svg'/>"+
                    "</div>"+
                    "<img class='study-view-drag-icon' src='images/move.svg'/>"+
                    "<span chartID='"+divs.tableId+"' class='study-view-dc-chart-delete'>x</span>"+
                "</div>"+
                "<chartTitleH4 id='"+divs.titleId+"'>Mutated Genes</chartTitleH4>" +
            "</div>"+
            "<div id='"+divs.tableId+"'>"+
            "</div>"+
            "<div id='"+divs.loaderId+"' class='study-view-loader' style='top:20%;left:20%'><img src='images/ajax-loader.gif'/></div>"+
        "</div>"
        $('#' + divs.attachedId).append(_div);
    }
    
    function initTable(data) {
        var arr = data.arr,
            attr = data.attr,
            table = $('#' + divs.tableId);
    
        var tableHtml = '<table><thead><tr></tr></thead><tbody></tbody></table>';
        table.html(tableHtml);
        
        var tableHeader = table.find('table thead tr');
        
        attr.forEach(function(e, i) {
            tableHeader.append('<th>'+ e.displayName +'</th>');
        });
        
        var tableBody = table.find('tbody');
        
        arr.forEach(function(e, i){
            var _row= '<tr>';
            attr.forEach(function(e1, i1){
                _row += '<td>' + e[e1.name] + '</td>';
            });
            _row += '</tr>';
            tableBody.append(_row);
        });
    }
    
    function initDataTable() {
        dataTable = $('#'+ divs.tableId +' table').dataTable({
            "sDom": 'rt<f>',
            "sScrollY": '255',
            "bPaginate": false,
            "aaSorting": [[1, 'desc']],
            "bAutoWidth": true,
            "fnInitComplete": function(oSettings, json) {
                $('#'+ divs.tableId +' .dataTables_filter')
                        .find('label')
                        .contents()
                        .filter(function(){
                            return this.nodeType === 3;
                        }).remove();

                $('#'+ divs.tableId +' .dataTables_filter')
                        .find('input')
                        .attr('placeholder', 'Search...');
            }
        });
    }
    
    function redraw(data) {
        dataTable.api().destroy();
        $('#' + divs.tableId).empty();
        initData(data);
        initTable(data);
        initDataTable();
    }
    
    return {
        init: init,
        getDataTable: function() {
            return dataTable;
        },
        redraw: redraw,
        getInitStatus: function(){
            return initStatus;
        }
    };
};