var oneGeneUtil = (function() {

    function isEmpty(inputVal) {
        if (inputVal !== "NaN" && inputVal !== "NA") {
            return false;
        }
        return true;
    }

    function plotsTypeIsCopyNo() {
        return userSelection.plots_type === "mrna_vs_copy_no";
    };

    function plotsTypeIsMethylation() {
        return userSelection.plots_type === "mrna_vs_dna_methylation";
    };

    function plotsTypeIsRPPA() {
        return userSelection.plots_type === "rppa_protein_level_vs_mrna";
    };

    function plotsIsDiscretized() {
        return userSelection.plots_type.indexOf("copy_no") !== -1 &&
            userSelection.copy_no_type.indexOf("log2") === -1 &&
            (userSelection.copy_no_type.indexOf("gistic") !== -1 ||
                userSelection.copy_no_type.indexOf("cna") !== -1 ||
                userSelection.copy_no_type.indexOf("CNA") !== -1);
    }

    function analyseData(inputArr) {
        var tmp_xData = [];
        var tmp_xIndex = 0;
        var tmp_yData = [];
        var tmp_yIndex = 0;
        for (var j = 0; j< inputArr.length; j++){
            if (!isEmpty(inputArr[j].xVal) && !isEmpty(inputArr[j].yVal)) {
                tmp_xData[tmp_xIndex] = inputArr[j].xVal;
                tmp_xIndex += 1;
                tmp_yData[tmp_yIndex] = inputArr[j].yVal;
                tmp_yIndex += 1;
            }
        }
        var min_x = Math.min.apply(Math, tmp_xData);
        var max_x = Math.max.apply(Math, tmp_xData);
        var edge_x = (max_x - min_x) * 0.2;
        var min_y = Math.min.apply(Math, tmp_yData);
        var max_y = Math.max.apply(Math, tmp_yData);
        var edge_y = (max_y - min_y) * 0.1;
        return {
            min_x: min_x,
            max_x: max_x,
            edge_x: edge_x,
            min_y: min_y,
            max_y: max_y,
            edge_y: edge_y
        };
    }

    function copyData(desArray, srcArray) {
        desArray.length = 0;
        var desArrayIndex = 0;
        for (var tmpIndex = 0; tmpIndex < srcArray.length; tmpIndex ++ ){
            if (srcArray[tmpIndex] !== "" && srcArray[tmpIndex] !== null ) {
                desArray[desArrayIndex] = srcArray[tmpIndex];
                desArrayIndex += 1;
            }
        }
    }

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

    return {
        plotsTypeIsCopyNo: plotsTypeIsCopyNo,
        plotsTypeIsMethylation: plotsTypeIsMethylation,
        plotsTypeIsRPPA: plotsTypeIsRPPA,
        isEmpty: isEmpty,
        copyData: copyData,
        plotsIsDiscretized: plotsIsDiscretized,
        analyseData: analyseData,
        searchIndexBottom: searchIndexBottom,
        searchIndexTop: searchIndexTop
    };

}());