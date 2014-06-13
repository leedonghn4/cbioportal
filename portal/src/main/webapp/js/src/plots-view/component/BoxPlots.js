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

/****************************************************************************************************
 * Calculating and Rendering boxplots for the input dataset
 * @author Yichao Sun / Eduardo
 * @date Jun 13
 *
 * @input: dataAttr -- the attributes of the input dataset (min val, max val, xScale, yScale, etc.)
 *         dotsGroup -- array of datum with info about every datapoints 
 *         boxPlotsElem -- DOM which the box plots would attach to
 *         positionIndicator -- 0, 1, 2, .... (# CNA groups - 1)
 *
 ****************************************************************************************************/

var BoxPlots = function() {

    //Value/attributes for the box plots
    var top;
    var bottom;
    var quan1;
    var quan2;
    var mean;
    var IQR;
    var midLine;
    //Data container
    var scaled_y_arr=[]; //y values with axis scale applied
    var tmp_y_arr = []; 

    var util = (function() {

        function searchIndexTop(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (ele <= arr[i]) {
                    return i;
                } else {
                    continue;
                }
            }
            return arr.length - 1;
        };

        function searchIndexBottom(arr, ele) {
            for(var i = 0; i < arr.length; i++) {
                if (parseFloat(ele) > parseFloat(arr[i])) {
                    continue ;
                } else if (parseFloat(ele) == parseFloat(arr[i])) {
                    return i;
                } else {
                    return i - 1;
                }
            }
            return arr.length - 1 ;
        };

        return {
            searchIndexTop: searchIndexTop,
            searchIndexBottom: searchIndexBottom
        }    
    }());

    function calc(dataAttr, dataArr, boxPlotsElem, positionIndicator) {
        //Find the middle (vertical) line for one box plot
        midLine = dataAttr.xScale(positionIndicator);
        dataArr.sort(function(a, b) { return a - b });
        if (dataArr.length === 0) {
            //Do nothing: DO NOT MOVE POSITION INDEX (pos)
        } else if (dataArr.length === 1) {
            mean = dataAttr.yScale(dataArr[0]);
            boxPlotsElem.append("line")
                .attr("x1", midLine-30)
                .attr("x2", midLine+30)
                .attr("y1", mean)
                .attr("y2", mean)
                .attr("stroke-width", 2)
                .attr("stroke", "grey");
        } else {
            if (dataArr.length === 2) {
                mean = dataAttr.yScale((dataArr[0] + dataArr[1]) / 2);
                quan1 = bottom = dataAttr.yScale(dataArr[0]);
                quan2 = top = dataAttr.yScale(dataArr[1]);
                IQR = Math.abs(quan2 - quan1);
            } else {
                var yl = dataArr.length;
                if (yl % 2 === 0) {
                    mean = dataAttr.yScale((dataArr[(yl / 2)-1] + dataArr[yl / 2]) / 2);
                    if (yl % 4 === 0) {
                        quan1 = dataAttr.yScale((dataArr[(yl / 4)-1] + dataArr[yl / 4]) / 2);
                        quan2 = dataAttr.yScale((dataArr[(3*yl / 4)-1] + dataArr[3 * yl / 4]) / 2);
                    } else {
                        quan1 = dataAttr.yScale(dataArr[Math.floor(yl / 4)]);
                        quan2 = dataAttr.yScale(dataArr[Math.floor(3 * yl / 4)]);
                    }
                } else {
                    mean = dataAttr.yScale(dataArr[Math.floor(yl / 2)]);
                    var tmp_yl = Math.floor(yl / 2) + 1;
                    if (tmp_yl % 2 === 0) {
                        quan1 = dataAttr.yScale((dataArr[tmp_yl / 2 - 1] + dataArr[tmp_yl / 2]) / 2);
                        quan2 = dataAttr.yScale((dataArr[(3 * tmp_yl / 2) - 2] + dataArr[(3 * tmp_yl / 2) - 1]) / 2);
                    } else {
                        quan1 = dataAttr.yScale(dataArr[Math.floor(tmp_yl / 2)]);
                        quan2 = dataAttr.yScale(dataArr[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                    }
                }
                for (var k = 0 ; k < dataArr.length ; k++) {
                    scaled_y_arr[k] = parseFloat(dataAttr.yScale(dataArr[k]));
                }
                scaled_y_arr.sort(function(a,b) { return a-b });
                IQR = Math.abs(quan2 - quan1);
                var index_top = util.searchIndexTop(scaled_y_arr, (quan2 - 1.5 * IQR));
                top = scaled_y_arr[index_top];
                var index_bottom = util.searchIndexBottom(scaled_y_arr, (quan1 + 1.5 * IQR));
                bottom = scaled_y_arr[index_bottom];
            }
            draw(boxPlotsElem);
        }
    }

    function draw(boxPlotsElem) {
        boxPlotsElem.append("rect")
            .attr("x", midLine-40)
            .attr("y", quan2)
            .attr("width", 80)
            .attr("height", IQR)
            .attr("fill", "none")
            .attr("stroke-width", 1)
            .attr("stroke", "#BDBDBD");
        boxPlotsElem.append("line")
            .attr("x1", midLine-40)
            .attr("x2", midLine+40)
            .attr("y1", mean)
            .attr("y2", mean)
            .attr("stroke-width", 2)
            .attr("stroke", "#BDBDBD");
        boxPlotsElem.append("line")
            .attr("x1", midLine-30)
            .attr("x2", midLine+30)
            .attr("y1", top)
            .attr("y2", top)
            .attr("stroke-width", 1)
            .attr("stroke", "#BDBDBD");
        boxPlotsElem.append("line")
            .attr("x1", midLine-30)
            .attr("x2", midLine+30)
            .attr("y1", bottom)
            .attr("y2", bottom)
            .attr("stroke", "#BDBDBD")
            .style("stroke-width", 1);
        boxPlotsElem.append("line")
            .attr("x1", midLine)
            .attr("x2", midLine)
            .attr("y1", quan1)
            .attr("y2", bottom)
            .attr("stroke", "#BDBDBD")
            .attr("stroke-width", 1);
        boxPlotsElem.append("line")
            .attr("x1", midLine)
            .attr("x2", midLine)
            .attr("y1", quan2)
            .attr("y2", top)
            .attr("stroke", "#BDBDBD")
            .style("stroke-width", 1);                 
    }

    return {
        init: function(dataAttr, dataArr, boxPlotsElem, positionIndicator) {
            calc(dataAttr, dataArr, boxPlotsElem, positionIndicator);
        }    
    } 

}
 