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
  * Generate the control menu on the left side for the plots tab
  * Contains three sub-views: one gene, two genes, and custom
  * Jun 2014
  * @Author: Yichao S/ Eduardo Velasco
  *
  * @Input:
  * @Ouput: 
  */ 
var PlotsMenu = (function () {
    var tabType = {
            ONE_GENE: "one_gene",
            TWO_GENES: "two_genes",
            CUSTOM: "custom"
        };
    var oneGene = {
            plot_type : {
                MRNA_COPY_NO : {
                    value : "mrna_vs_copy_no",
                    text : "mRNA vs. Copy Number",
                    dataTypeX: "MRNA", //put data type key here as the indicator of a profile
                    dataTypeY: "COPY_NO"
                },
                MRNA_METHYLATION : {
                    value : "mrna_vs_dna_methylation",
                    text : "mRNA vs. DNA Methylation",
                    dataTypeX: "MRNA",
                    dataTypeY: "METHYLATION"
                },
                RPPA_MRNA : {
                    value : "rppa_vs_mrna",
                    text : "RPPA Protein Level vs. mRNA",
                    dataTypeX: "RPPA",
                    dataTypeY: "MRNA"
                }
            },
            data_type : {
                MRNA : {
                    id: "one_gene_data_type_mrna",
                    label: "- mRNA -",
                    value: "mrna",
                    genetic_profile : []
                },
                COPY_NO : {
                    id: "one_gene_data_type_copy_no",
                    label: "- Copy Number -",
                    value: "copy_no",
                    genetic_profile : []
                },
                METHYLATION : {
                    id: "one_gene_data_type_methylation",
                    label: "- DNA Methylation -",
                    value: "dna_methylation",
                    genetic_profile : []
                },
                RPPA : {
                    id: "one_gene_data_type_rppa",
                    label: "- RPPA Protein Level -",
                    value: "rppa",
                    genetic_profile : []
                }                
            },
            status : {
                has_mrna : false,
                has_dna_methylation : false,
                has_rppa : false,
                has_copy_no : false
            }
        },
        twoGenes = {
            plot_type : {
                MRNA : { 
                    value : "mrna", 
                    name :  "mRNA Expression",
                    data_type: "MRNA" //put data type key here as the indicator of a profile
                },
                COPY_NO : { 
                    value : "copy_no", 
                    name :  "Copy Number Alteration",
                    data_type: "COPY_NO" //put data type key here as the indicator of a profile
                },
                METHYLATION : { 
                    value : "methylation", 
                    name :  "DNA Methylation",
                    data_type: "METHYLATION" //put data type key here as the indicator of a profile
                },
                RPPA : { 
                    value : "rppa", 
                    name :  "RPPA Protein Level",
                    data_type: "RPPA" //put data type key here as the indicator of a profile
                }                
            },
            data_type : {  //Only contain genetic profiles that has data available for both genes
                // "mutations" : {
                //     genetic_profile : []
                // },
                MRNA : {
                    value: "mrna",
                    genetic_profile : []
                },
                COPY_NO : {
                    value: "copy_no",
                    genetic_profile : []
                },
                METHYLATION : {
                    value: "methylation",
                    genetic_profile : []
                },
                RPPA : {
                    value: "rppa",
                    genetic_profile : []
                }                  
            }
        };

    function fetchContentOneGene(selectedGene) {
        oneGene.data_type.MRNA.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_mrna;
        oneGene.data_type.COPY_NO.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_copy_no;
        oneGene.data_type.METHYLATION.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_dna_methylation;
        oneGene.data_type.RPPA.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_rppa;
        oneGene.status.has_mrna = (oneGene.data_type.MRNA.genetic_profile.length !== 0);
        oneGene.status.has_copy_no = (oneGene.data_type.COPY_NO.genetic_profile.length !== 0);
        oneGene.status.has_dna_methylation = (oneGene.data_type.METHYLATION.genetic_profile.length !== 0);
        oneGene.status.has_rppa = (oneGene.data_type.RPPA.genetic_profile.length !== 0);
    }

    function fetchContentTwoGenes(selectedGenes) {
        var geneX = selectedGenes[0], 
            geneY = selectedGenes[1];
        //content.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
        twoGenes.data_type.MRNA.genetic_profile = ControlPanelUtil.mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_mrna,
            Plots.getGeneticProfiles(geneY).genetic_profile_mrna
        );
        twoGenes.data_type.COPY_NO.genetic_profile = ControlPanelUtil.mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_copy_no,
            Plots.getGeneticProfiles(geneY).genetic_profile_copy_no
        );
        twoGenes.data_type.METHYLATION.genetic_profile = ControlPanelUtil.mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation,
            Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation
        );
        twoGenes.data_type.RPPA.genetic_profile = ControlPanelUtil.mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_rppa,
            Plots.getGeneticProfiles(geneY).genetic_profile_rppa
        );
    }

    function drawOneGeneGeneList() {
        ControlPanelUtil.generateGeneList("one_gene_gene_list", gene_list);
        $("#one_gene_gene_list").on("change", function() {
            drawOneGenePlotType();
            drawOneGeneDataType();    
        } );
    }

    function drawOneGenePlotType() {
        $("#one_gene_plot_type_div").empty();
        $("#one_gene_plot_type_div").append("<select id='one_gene_plot_type'></select>");
        $("#one_gene_plot_type").on("change", drawOneGeneDataType);
        if (oneGene.status.has_mrna && oneGene.status.has_copy_no) {
            ControlPanelUtil.appendDropDown(
                '#one_gene_plot_type',
                oneGene.plot_type.MRNA_COPY_NO.value,
                oneGene.plot_type.MRNA_COPY_NO.text
            );
        }
        if (oneGene.status.has_mrna && oneGene.status.has_dna_methylation) {
            ControlPanelUtil.appendDropDown(
                '#one_gene_plot_type',
                oneGene.plot_type.MRNA_METHYLATION.value,
                oneGene.plot_type.MRNA_METHYLATION.text
            );
        }
        if (oneGene.status.has_mrna && oneGene.status.has_rppa) {
            ControlPanelUtil.appendDropDown(
                '#one_gene_plot_type',
                oneGene.plot_type.RPPA_MRNA.value,
                oneGene.plot_type.RPPA_MRNA.text
            );
        }
    }

    function drawOneGeneDataType() {
        $('#one_gene_data_type_div').empty();
        var currentPlotsType = $('#one_gene_plot_type').val();
        var _selectedDataTypeNames = [];
        for (var key in oneGene.plot_type) {
            if (currentPlotsType === oneGene.plot_type[key].value) { //Get the two selected data type
                _selectedDataTypeNames[0] = oneGene.plot_type[key].dataTypeX;
                _selectedDataTypeNames[1] = oneGene.plot_type[key].dataTypeY;
            }
        }
        $.each(_selectedDataTypeNames, function(index, dataTypeName) {
            var _dataTypeObj = oneGene.data_type[dataTypeName];
            $("#one_gene_data_type_div").append(
                "<label for='" + _dataTypeObj.id + "'>" + _dataTypeObj.label + "</label><br>" +
                "<select id='" + _dataTypeObj.id + "' onchange='' class='plots-select'></select><br>"
            );  
            for (var index in _dataTypeObj.genetic_profile) { //genetic_profile is an ARRAY!
                var item_profile = _dataTypeObj.genetic_profile[index];
                $("#" + _dataTypeObj.id).append(
                    "<option value='" + item_profile[0] + "|" + item_profile[2] + "'>" + item_profile[1] + "</option>");
            }          
        });
    }

    function setOneGeneDataTypeSel() {
        //-----Copy No Priority List: discretized(gistic, rae), continuous
        $('#data_type_copy_no > option').each(function() {
            if (this.text.toLowerCase().indexOf("(rae)") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.text.toLowerCase().indexOf("gistic") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        var userSelectedCopyNoProfile = "";
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("cna") !== -1 || value.indexOf("log2") !== -1 ||
                value.indexOf("CNA")!== -1 || value.indexOf("gistic") !== -1) {
                userSelectedCopyNoProfile = value;
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.value === userSelectedCopyNoProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
        //----mRNA Priority List: User selection, RNA Seq V2, RNA Seq, Z-scores
        var userSelectedMrnaProfile = "";  //Get user selection from main query
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("mrna") !== -1) {
                userSelectedMrnaProfile = value;
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("z-scores") !== -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.value === userSelectedMrnaProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
        //----DNA Methylation Priority List: hm450, others
        $('#data_type_dna_methylation > option').each(function() {
            if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });        
    }

    function drawTwoGenesGeneList() {
        ControlPanelUtil.generateGeneList("two_genes_gene_list_x", gene_list);
        var tmp_gene_list = jQuery.extend(true, [], gene_list);
        var tmp_gene_holder = tmp_gene_list.pop(); //Move the last gene on the list to the first
        tmp_gene_list.unshift(tmp_gene_holder);
        ControlPanelUtil.generateGeneList("two_genes_gene_list_y", tmp_gene_list);
        $("#two_genes_gene_list_x").on("change", drawTwoGenesPlotType);
        $("#two_genes_gene_list_x").on("change", drawTwoGenesDataType);
        $("#two_genes_gene_list_y").on("change", drawTwoGenesPlotType);
        $("#two_genes_gene_list_y").on("change", drawTwoGenesDataType);
    }

    function drawTwoGenesPlotType() {
        $("#two_genes_plot_type_div").empty();
        $("#two_genes_plot_type_div").append("<select id='two_genes_plot_type'></select>");
        $("#two_genes_plot_type").on("change", drawTwoGenesDataType);
        ControlPanelUtil.appendDropDown("#two_genes_plot_type", twoGenes.plot_type.MRNA.value, twoGenes.plot_type.MRNA.name);
        if (twoGenes.data_type.COPY_NO.genetic_profile.length !== 0) {
            var _flag = false;
            $.each(twoGenes.data_type.COPY_NO.genetic_profile, function(index, val) {
                if (!ControlPanelUtil.dataIsDiscretized(val[1])) { //Only shown if having continous copy no genetic profile available
                    _flag = true;
                }
            });     
            if (_flag) {
                ControlPanelUtil.appendDropDown("#two_genes_plot_type", twoGenes.plot_type.COPY_NO.value, twoGenes.plot_type.COPY_NO.name);
            }
        }
        if (twoGenes.data_type.METHYLATION.length !== 0) {
           ControlPanelUtil.appendDropDown("#two_genes_plot_type", twoGenes.plot_type.METHYLATION.value, twoGenes.plot_type.METHYLATION.name);
        }
        if (twoGenes.data_type.RPPA.length !== 0) {
           ControlPanelUtil.appendDropDown("#two_genes_plot_type", twoGenes.plot_type.RPPA.value, twoGenes.plot_type.RPPA.name);
        }
    }

    function drawTwoGenesDataType() {
        $("#two_genes_data_type_div").empty();
        $("#two_genes_data_type_div").append(
            "<select id='two_genes_data_type' " + 
            "onchange='' class='plots-select'>");
        var _currentSelectedPlotType = $("#two_genes_plot_type").val();
        for (var key in twoGenes.plot_type) {
            if (_currentSelectedPlotType === twoGenes.plot_type[key].value) {
                var _currentSelectedDataTypeKey = twoGenes.plot_type[key].data_type;
                twoGenes.data_type[_currentSelectedDataTypeKey].genetic_profile.forEach(function(profile) {
                    if (!ControlPanelUtil.dataIsDiscretized(profile[1])) {
                        $("#two_genes_data_type")
                            .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");                    
                    }
                }); 
            }
        }
    }

    function setTwoGenesDataTypeSel() {
        //----mRNA Priority List: RNA Seq V2, RNA Seq, Z-scores
        if ($("#two_genes_plot_type").val() === values.MRNA) {
            $("#two_genes_data_type > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores")){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#two_genes_data_type > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#two_genes_data_type > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }
        //----DNA methylation priority list: hm450, hm27
        if ($("#two_genes_plot_type").val() === values.METHYLATION) {
            $('#two_genes_data_type > option').each(function() {
                if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }
    }

    function drawErrMsgs() {
        $("#one_gene_type_specification").hide();
        $("#menu_err_msg").append("<h5>Profile data missing for generating this view.</h5>");
    }

    function setDefaultMrnaSelection() {
        var userSelectedMrnaProfile = "";  //from main query
        //geneticProfiles --> global variable, passing user selected profile IDs
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("mrna") !== -1) {
                userSelectedMrnaProfile = value;
                return false;
            }
        });

        //----Priority List: User selection, RNA Seq V2, RNA Seq, Z-scores
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("z-scores") !== -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.value === userSelectedMrnaProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function setDefaultMethylationSelection() {
        $('#data_type_dna_methylation > option').each(function() {
            if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function updateVisibility() {
        $("#one_gene_log_scale_x_div").remove();
        $("#one_gene_log_scale_y_div").remove();
        var currentPlotsType = $('#plots_type').val();
        if (currentPlotsType.indexOf("copy_no") !== -1) {
            Util.toggleVisibilityX("data_type_copy_no_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
        } else if (currentPlotsType.indexOf("dna_methylation") !== -1) {
            Util.toggleVisibilityX("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
        } else if (currentPlotsType.indexOf("rppa") !== -1) {
            Util.toggleVisibilityX("data_type_mrna_dropdown");
            Util.toggleVisibilityY("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
        }
        updateLogScaleOption();
    }

    function updateLogScaleOption() {
        $("#one_gene_log_scale_x_div").empty();
        $("#one_gene_log_scale_y_div").empty();
        var _str_x = "<input type='checkbox' id='log_scale_option_x' checked onchange='PlotsView.applyLogScaleX();'/> log scale";
        var _str_y = "<input type='checkbox' id='log_scale_option_y' checked onchange='PlotsView.applyLogScaleY();'/> log scale";
        if ($("#plots_type").val() === content.one_gene_tab_plots_type.mrna_copyNo.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.one_gene_tab_plots_type.mrna_methylation.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.one_gene_tab_plots_type.rppa_mrna.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_x_div").append(_str_x);
            }
        }
    }

    return {
        init: function () {
            $("#menu_err_msg").empty();

            // ---- One Gene Sub Tab ----
            drawOneGeneGeneList();
            fetchContentOneGene(gene_list[0]);
            drawOneGenePlotType();
            drawOneGeneDataType();
            setOneGeneDataTypeSel();

            // ---- Two Genes Sub Tab ----
            if (gene_list.length > 2) {
                drawTwoGenesGeneList();               
                fetchContentTwoGenes([gene_list[0], gene_list[1]]);
                drawTwoGenesPlotType();
                drawTwoGenesDataType();
                setTwoGenesDataTypeSel();
            }

            //drawMenuTwoGenes();
            // if (oneGene.status.has_mrna && 
            //    (oneGene.status.has_copy_no || 
            //     oneGene.status.has_dna_methylation || 
            //     oneGene.status.has_rppa)) {
            //         drawMenu();
            //         setDefaultMrnaSelection();
            //         setDefaultCopyNoSelection();
            //         setDefaultMethylationSelection();
            //         updateVisibility();
            // } else {
            //     drawErrMsgs();
            // }
        },
        updateMenu: function() {
            $("#menu_err_msg").empty();
            fetchFrameContent(document.getElementById("gene").value);
            if(status.has_mrna && (status.has_copy_no || status.has_dna_methylation || status.has_rppa)) {
                drawMenu();
                setDefaultMrnaSelection();
                setDefaultCopyNoSelection();
                setDefaultMethylationSelection();
                updateVisibility();
            } else {
                drawErrMsgs();
            }
        },
        updateDataType: function() {
            setDefaultMrnaSelection();
            setDefaultCopyNoSelection();
            setDefaultMethylationSelection();
            updateVisibility();
        },
        updateLogScaleOption: updateLogScaleOption,
        getStatus: function() {
            return status;
        }
    };
}()); //Closing PlotsMenu