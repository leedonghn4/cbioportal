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
  * June 2014
  * @author: Yichao Sun <yichao@cbio.mskcc.org>
  */

var PlotsTabMenuUtil = (function() {

    function mergeList(arrX, arrY) {
        var result = [];
        var _arrY = [];
        $.each(arrY, function(index, val) {
            _arrY.push(val[0]);
        });
        $.each(arrX, function(index, val) {
            if (_arrY.indexOf(val[0]) !== -1) {
                result.push(arrX[index]);
            }
        });
        return result;
    }

    function appendDropDown(divId, value, text) {
        $(divId).append("<option value='" + value + "'>" + text + "</option>");
    }

    function toggleVisibilityX(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'block';
        $("#" + elemId).append("<div id='one_gene_log_scale_x_div'></div>");
    }

    function toggleVisibilityY(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'block';
        $("#" + elemId).append("<div id='one_gene_log_scale_y_div'></div>");
    }

    function toggleVisibilityHide(elemId) {
        var e = document.getElementById(elemId);
        e.style.display = 'none';
    }

    function generateGeneList(elemId, options) {
        var select = document.getElementById(elemId);
        options.forEach(function(option) {
            var el = document.createElement("option");
            el.textContent = option;
            el.value = option;
            select.appendChild(el);                
        });
    }

    function dataIsDiscretized(profileText) {
        if (profileText.indexOf("GISTIC") !== -1 ||
            profileText.indexOf("RAE") !== -1 ||
            profileText.indexOf("discretization") !== -1) {
            return true;
        }

        return false;
    }

    return {
        appendDropDown: appendDropDown,
        toggleVisibilityX: toggleVisibilityX,
        toggleVisibilityY: toggleVisibilityY,
        toggleVisibilityHide: toggleVisibilityHide,
        generateGeneList: generateGeneList,
        mergeList: mergeList,
        dataIsDiscretized: dataIsDiscretized
    };
}());