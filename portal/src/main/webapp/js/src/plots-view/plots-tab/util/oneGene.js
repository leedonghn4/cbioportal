var oneGeneUtil = (function() {

    function isEmpty(inputVal) {
        if (inputVal !== "NaN" && inputVal !== "NA" && 
            inputVal !== "" && inputVal !== null) {
            return false;
        }
        return true;
    }

    function plotsIsDiscretized(_dataTypeObj) {
        var result = false;
        for(var key in _dataTypeObj) {
            var _id = _dataTypeObj[key].id.toLowerCase();
            if (_id.indexOf("log2") === -1 && 
                (_id.indexOf("gistic") !== -1 ||
                 _id.indexOf("cna") !== -1)) {
                    result = true;                    
            }
        }
        return result;
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
        isEmpty: isEmpty,
        copyData: copyData,
        plotsIsDiscretized: plotsIsDiscretized,
        analyseData: analyseData,
        searchIndexBottom: searchIndexBottom,
        searchIndexTop: searchIndexTop
    };

}());