   var ControlPanelUtil = (function() {

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