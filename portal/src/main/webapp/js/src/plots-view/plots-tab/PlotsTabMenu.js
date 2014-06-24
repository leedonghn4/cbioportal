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
  *
  * @Author: Yichao Sun <yichao@cbio.mskcc.org>/ Eduardo Velasco
  */ 

var PlotsTabMenu = (function () {

    var OneGeneMenu = (function() {

        var plot_type = {
                MRNA_COPY_NO : {
                    value : "mrna_vs_copy_no",
                    text : "mRNA vs. Copy Number",
                    data_type_x: "MRNA", //put data type key here as the indicator of a profile
                    data_type_y: "COPY_NO"
                },
                MRNA_METHYLATION : {
                    value : "mrna_vs_dna_methylation",
                    text : "mRNA vs. DNA Methylation",
                    data_type_x: "MRNA", //put data type key here as the indicator of a profile
                    data_type_y: "METHYLATION"
                },
                RPPA_MRNA : {
                    value : "rppa_vs_mrna",
                    text : "RPPA Protein Level vs. mRNA",
                    data_type_x: "RPPA", //put data type key here as the indicator of a profile
                    data_type_y: "MRNA"
                }
            },
            data_type = {
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
            status = {
                has_mrna : false,
                has_dna_methylation : false,
                has_rppa : false,
                has_copy_no : false
            }

        function fetchContent(selectedGene) {
            data_type.MRNA.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_mrna;
            data_type.COPY_NO.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_copy_no;
            data_type.METHYLATION.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_dna_methylation;
            data_type.RPPA.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_rppa;
            status.has_mrna = (data_type.MRNA.genetic_profile.length !== 0);
            status.has_copy_no = (data_type.COPY_NO.genetic_profile.length !== 0);
            status.has_dna_methylation = (data_type.METHYLATION.genetic_profile.length !== 0);
            status.has_rppa = (data_type.RPPA.genetic_profile.length !== 0);
        }

        function drawGeneList() {
            PlotsTabMenuUtil.generateGeneList("one_gene_gene_list", gene_list);
            $("#one_gene_gene_list").on("change", function() {
                drawPlotType();
                drawDataType();
                setDataTypeSel();
                drawLogScale();    
            } );
        }

        function drawPlotType() {
            $("#one_gene_plot_type_div").empty();
            $("#one_gene_plot_type_div").append("<select id='one_gene_plot_type'></select>");
            $("#one_gene_plot_type").on("change", function() {
                drawDataType();
                setDataTypeSel();
                drawLogScale();       
            });
            if (status.has_mrna && status.has_copy_no) {
                PlotsTabMenuUtil.appendDropDown(
                    '#one_gene_plot_type',
                    plot_type.MRNA_COPY_NO.value,
                    plot_type.MRNA_COPY_NO.text
                );
            }
            if (status.has_mrna && status.has_dna_methylation) {
                PlotsTabMenuUtil.appendDropDown(
                    '#one_gene_plot_type',
                    plot_type.MRNA_METHYLATION.value,
                    plot_type.MRNA_METHYLATION.text
                );
            }
            if (status.has_mrna && status.has_rppa) {
                PlotsTabMenuUtil.appendDropDown(
                    '#one_gene_plot_type',
                    plot_type.RPPA_MRNA.value,
                    plot_type.RPPA_MRNA.text
                );
            }
        }

        function drawDataType() {
            $('#one_gene_data_type_div').empty();
            var currentPlotsType = $('#one_gene_plot_type').val();
            var _selectedDataTypeNames = [];
            for (var key in plot_type) {
                if (currentPlotsType === plot_type[key].value) { //Get the two selected data type
                    _selectedDataTypeNames[0] = plot_type[key].data_type_x;
                    _selectedDataTypeNames[1] = plot_type[key].data_type_y;
                }
            }
            $.each(_selectedDataTypeNames, function(dataTypeIndex, dataTypeName) {
                var _dataTypeObj = data_type[dataTypeName];
                $("#one_gene_data_type_div").append(
                    "<label for='" + _dataTypeObj.id + "'>" + _dataTypeObj.label + "</label><br>" +
                    "<select id='" + _dataTypeObj.id + "' class='plots-select'></select>" +
                    "<div id='" + _dataTypeObj.id + "_log_scale_div'></div>" 
                );  
                $("#" + _dataTypeObj.id).on("change", drawLogScale);
                for (var index in _dataTypeObj.genetic_profile) { //genetic_profile is an ARRAY!
                    var item_profile = _dataTypeObj.genetic_profile[index];
                    $("#" + _dataTypeObj.id).append(
                        "<option value='" + item_profile[0] + "|" + item_profile[2] + "'>" + item_profile[1] + "</option>");
                }  
                $("#one_gene_data_type_div").append("<br>");
            });
        }

        function setDataTypeSel() {
            //-----Copy No Priority List: discretized(gistic, rae), continuous
            $('#one_gene_data_type_copy_no > option').each(function() {
                if (this.text.toLowerCase().indexOf("(rae)") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#one_gene_data_type_copy_no > option").each(function() {
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
            $("#one_gene_data_type_copy_no > option").each(function() {
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
            $("#one_gene_data_type_mrna > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores") !== -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#one_gene_data_type_mrna > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#one_gene_data_type_mrna > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#one_gene_data_type_mrna > option").each(function() {
                if (this.value === userSelectedMrnaProfile){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            //----DNA Methylation Priority List: hm450, others
            $('#one_gene_data_type_methylation > option').each(function() {
                if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });        
        }

        function drawLogScale() {
            $("#one_gene_data_type_mrna_log_scale_div").empty();
            var currentPlotsType = $('#one_gene_plot_type').val();
            if (currentPlotsType === plot_type.MRNA_COPY_NO.value || 
                currentPlotsType === plot_type.MRNA_METHYLATION.value) {
                if ($("#one_gene_data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                    $("#one_gene_data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                        $("#one_gene_data_type_mrna_log_scale_div").append(
                            "<input type='checkbox' id='one_gene_log_scale_option_y' checked/> log scale");
                } 
            } else if (currentPlotsType === plot_type.RPPA_MRNA.value) {
                if ($("#one_gene_data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                    $("#one_gene_data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                        $("#one_gene_data_type_mrna_log_scale_div").append(
                            "<input type='checkbox' id='one_gene_log_scale_option_x' checked/> log scale");
                } 
            }
        }

        return {
            init : function() {
                $("#menu_err_msg").empty();
                drawGeneList();
                fetchContent(gene_list[0]);
                if (status.has_mrna && 
                   (status.has_copy_no || 
                    status.has_dna_methylation || 
                    status.has_rppa)) {
                        drawPlotType();
                        drawDataType();
                        setDataTypeSel();
                        drawLogScale();
                } else {
                    drawErrMsgs();
                }
            }            
        }

    }()); //Closing OneGeneMenu

    var TwoGenesMenu = (function() {
            
        var plot_type = {
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
            data_type = {  //Only contain genetic profiles that has data available for both genes
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

        function fetchContent(selectedGenes) {
            var geneX = selectedGenes[0], 
                geneY = selectedGenes[1];
            //content.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
            data_type.MRNA.genetic_profile = PlotsTabMenuUtil.mergeList(
                Plots.getGeneticProfiles(geneX).genetic_profile_mrna,
                Plots.getGeneticProfiles(geneY).genetic_profile_mrna
            );
            data_type.COPY_NO.genetic_profile = PlotsTabMenuUtil.mergeList(
                Plots.getGeneticProfiles(geneX).genetic_profile_copy_no,
                Plots.getGeneticProfiles(geneY).genetic_profile_copy_no
            );
            data_type.METHYLATION.genetic_profile = PlotsTabMenuUtil.mergeList(
                Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation,
                Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation
            );
            data_type.RPPA.genetic_profile = PlotsTabMenuUtil.mergeList(
                Plots.getGeneticProfiles(geneX).genetic_profile_rppa,
                Plots.getGeneticProfiles(geneY).genetic_profile_rppa
            );
        }
    
        function drawGeneList() {
            PlotsTabMenuUtil.generateGeneList("two_genes_gene_list_x", gene_list);
            var tmp_gene_list = jQuery.extend(true, [], gene_list);
            var tmp_gene_holder = tmp_gene_list.pop(); //Move the last gene on the list to the first
            tmp_gene_list.unshift(tmp_gene_holder);
            PlotsTabMenuUtil.generateGeneList("two_genes_gene_list_y", tmp_gene_list);
            $("#two_genes_gene_list_x").on("change", function() {
                drawPlotType();
                drawDataType();
                setDataTypeSel();
                drawLogScale();
            });
            $("#two_genes_gene_list_y").on("change", function() {
                drawPlotType();
                drawDataType();
                setDataTypeSel();
                drawLogScale();
            });
        }

        function drawPlotType() {
            $("#two_genes_plot_type_div").empty();
            $("#two_genes_plot_type_div").append("<select id='two_genes_plot_type'></select>");
            $("#two_genes_plot_type").on("change", function() {
                drawDataType();   
                setDataTypeSel();  
                drawLogScale();       
            });
            PlotsTabMenuUtil.appendDropDown("#two_genes_plot_type", plot_type.MRNA.value, plot_type.MRNA.name);
            if (data_type.COPY_NO.genetic_profile.length !== 0) {
                var _flag = false;
                $.each(data_type.COPY_NO.genetic_profile, function(index, val) {
                    if (!PlotsTabMenuUtil.dataIsDiscretized(val[1])) { //Only shown if having continous copy no genetic profile available
                        _flag = true;
                    }
                });     
                if (_flag) {
                    PlotsTabMenuUtil.appendDropDown("#two_genes_plot_type", plot_type.COPY_NO.value, plot_type.COPY_NO.name);
                }
            }
            if (data_type.METHYLATION.length !== 0) {
               PlotsTabMenuUtil.appendDropDown("#two_genes_plot_type", plot_type.METHYLATION.value, plot_type.METHYLATION.name);
            }
            if (data_type.RPPA.length !== 0) {
               PlotsTabMenuUtil.appendDropDown("#two_genes_plot_type", plot_type.RPPA.value, plot_type.RPPA.name);
            }
        }

        function drawDataType() {
            $("#two_genes_data_type_div").empty();
            $("#two_genes_data_type_div").append("<select id='two_genes_data_type' class='plots-select'>");
            var _currentSelectedPlotType = $("#two_genes_plot_type").val();
            for (var key in plot_type) {
                if (_currentSelectedPlotType === plot_type[key].value) {
                    var _currentSelectedDataTypeKey = plot_type[key].data_type;
                    data_type[_currentSelectedDataTypeKey].genetic_profile.forEach(function(profile) {
                        if (!PlotsTabMenuUtil.dataIsDiscretized(profile[1])) {
                            $("#two_genes_data_type")
                                .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");                    
                        }
                    }); 
                }
            }
            $("#two_genes_data_type").on("change", drawLogScale);
        }

        function setDataTypeSel() {
            //----mRNA Priority List: RNA Seq V2, RNA Seq, Z-scores
            if ($("#two_genes_plot_type").val() === plot_type.MRNA.value) {
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
            if ($("#two_genes_plot_type").val() === plot_type.METHYLATION.value) {
                $('#two_genes_data_type > option').each(function() {
                    if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                        $(this).prop('selected', true);
                        return false;
                    }
                });
            }
        }

        function drawLogScale() {
            $("#two_genes_log_scale_div_x").empty();
            $("#two_genes_log_scale_div_y").empty();
            var _str_x = "<input type='checkbox' id='two_genes_log_scale_option_x' checked onchange='PlotsTwoGenesView.updateLogScaleX();'/> log scale - x axis";
            var _str_y = "<input type='checkbox' id='two_genes_log_scale_option_y' checked onchange='PlotsTwoGenesView.updateLogScaleY();'/> log scale - y axis";
            if (($("#two_genes_plot_type").val() ===  plot_type.MRNA.value &&
                 $("#two_genes_data_type option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                 $("#two_genes_data_type option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
                $("#two_genes_log_scale_div_x").append(_str_x);
                $("#two_genes_log_scale_div_y").append(_str_y);
            }
        }

        function drawErrMsgs() {
            $("#one_gene_type_spec_div").hide();
            $("#menu_err_msg").append("<h5>Profile data missing for generating this view.</h5>");
        }

        return {
            init: function() {
                drawGeneList();               
                fetchContent([gene_list[0], gene_list[1]]);
                drawPlotType();
                drawDataType();
                setDataTypeSel();
                drawLogScale();
            }

        }

    }()); //Closing TwoGenesMenu

    var CustomMenu = (function() {
        var plot_type = {
                MRNA : { 
                    value : "mrna", 
                    name :  "mRNA Expression",
                    data_type : "MRNA" 
                },
                COPY_NO : { 
                    value : "copy_no", 
                    name :  "Copy Number Alteration",
                    data_type : "COPY_NO"
                },
                METHYLATION : { 
                    value : "methylation", 
                    name :  "DNA Methylation",
                    data_type : "METHYLATION" 
                },
                RPPA : { 
                    value : "rppa", 
                    name :  "RPPA Protein Level",
                    data_type : "RPPA"
                }
            },
            data_type = {
                gene_x : {
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
                },
                gene_y : {
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
            }

        function generateGeneList() {
            PlotsTabMenuUtil.generateGeneList("custom_gene_x", gene_list);
            var tmp_gene_list = jQuery.extend(true, [], gene_list);
            var tmp_gene_holder = tmp_gene_list.pop();
            tmp_gene_list.unshift(tmp_gene_holder);
            PlotsTabMenuUtil.generateGeneList("custom_gene_y", tmp_gene_list);
        }

        function fetchFrameData(geneX, geneY) {
            //data_type.gene_x.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
            data_type.gene_x.MRNA.genetic_profile = Plots.getGeneticProfiles(geneX).genetic_profile_mrna;
            data_type.gene_x.COPY_NO.genetic_profile = Plots.getGeneticProfiles(geneX).genetic_profile_copy_no;
            data_type.gene_x.METHYLATION.genetic_profile = Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation;
            data_type.gene_x.RPPA.genetic_profile = Plots.getGeneticProfiles(geneX).genetic_profile_rppa;
            //data_type.gene_y.genetic_profile_mutations = Plots.getGeneticProfiles(geneY).genetic_profile_mutations;
            data_type.gene_y.MRNA.genetic_profile = Plots.getGeneticProfiles(geneY).genetic_profile_mrna;
            data_type.gene_y.COPY_NO.genetic_profile = Plots.getGeneticProfiles(geneY).genetic_profile_copy_no;
            data_type.gene_y.METHYLATION.genetic_profile = Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation;
            data_type.gene_y.RPPA.genetic_profile = Plots.getGeneticProfiles(geneY).genetic_profile_rppa;
        }

        function drawPlotType() {
            PlotsTabMenuUtil.appendDropDown("#custom_plot_type_x", plot_type.MRNA.value, plot_type.MRNA.name);
            PlotsTabMenuUtil.appendDropDown("#custom_plot_type_y", plot_type.MRNA.value, plot_type.MRNA.name);
            if (data_type.gene_x.COPY_NO.genetic_profile.length !== 0) {
                var _flag = false;
                $.each(data_type.gene_x.COPY_NO.genetic_profile, function(index, val) {
                    if (!PlotsTabMenuUtil.dataIsDiscretized(val[1])) { //Only shown if having continous copy no genetic profile available
                        _flag = true;
                    }
                });     
                if (_flag) {
                    PlotsTabMenuUtil.appendDropDown(
                        "#custom_plot_type_x", plot_type.COPY_NO.value, plot_type.COPY_NO.name);
                }
            }
            if (data_type.gene_y.COPY_NO.genetic_profile.length !== 0) {
                var _flag = false;
                $.each(data_type.gene_y.COPY_NO.genetic_profile, function(index, val) {
                    if (!PlotsTabMenuUtil.dataIsDiscretized(val[1])) { //Only shown if having continous copy no genetic profile available
                        _flag = true;
                    }
                });     
                if (_flag) {
                    PlotsTabMenuUtil.appendDropDown(
                        "#custom_plot_type_y", plot_type.COPY_NO.value, plot_type.COPY_NO.name);
                }
            }
            if (data_type.gene_x.METHYLATION.genetic_profile.length !== 0) {
                PlotsTabMenuUtil.appendDropDown("#custom_plot_type_x", plot_type.METHYLATION.value, plot_type.METHYLATION.name);
            }
            if (data_type.gene_y.METHYLATION.genetic_profile.length !== 0) {
                PlotsTabMenuUtil.appendDropDown("#custom_plot_type_y", plot_type.METHYLATION.value, plot_type.METHYLATION.name);
            }
            if (data_type.gene_x.RPPA.genetic_profile.length !== 0) {
                PlotsTabMenuUtil.appendDropDown("#custom_plot_type_x", plot_type.RPPA.value, plot_type.RPPA.name);
            }
            if (data_type.gene_x.RPPA.genetic_profile.length !== 0) {
                PlotsTabMenuUtil.appendDropDown("#custom_plot_type_y", plot_type.RPPA.value, plot_type.RPPA.name);
            }
        }


        return {
            init : function() {
                generateGeneList();
                if (gene_list.length >= 2) {
                    fetchFrameData(gene_list[0], gene_list[1]);
                } else {
                    fetchFrameData(gene_list[0], gene_list[0]);
                }
                drawPlotType();
            }
        }


    }());

    return {
        init : function() {
            OneGeneMenu.init();
            if (gene_list.length >= 2) {
                TwoGenesMenu.init();
            }  
            CustomMenu.init();          
        }
    }

}()); //Closing PlotsMenu