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

/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// This is for the moustache-like templates
// prevents collisions with JSP tags <%...%>
_.templateSettings = {
    interpolate : /\{\{(.+?)\}\}/g
};

// Router
AppRouter = Backbone.Router.extend({
    routes: {
        "*actions": "home"
    },

    home: function(actions) {
        var loadingView = new LoadingStudiesView();
        loadingView.render();
        // First load the metadata

        $.get("cross_cancer.json",
            { data_priority: dataPriority },
            function(data) {
                var studiesLoadedView = new StudiesLoadedView({ model: data });
                studiesLoadedView.render();
            }
        );

        return this;
    }

});

// Models

// Views
LoadingStudiesView = Backbone.View.extend({
    el: $("#results_container"),
    render: function() {
        return this;
    }
});

StudiesLoadedView = Backbone.View.extend({
    el: $("#results_container"),
    render: function() {
        var studiesEl = this.el;
        $(studiesEl).html("");

        var oncoPrintKeyView = new OncoPrintKeyView({ el: "#oncoprint_key_container" });
        oncoPrintKeyView.render();

        var i = 0;
        _.each(this.model, function(aStudy) {
            i++;
            if(i < 5) {
                var oncoprintView = new OncoprintView({ model: aStudy, el: studiesEl });
                oncoprintView.render();
            }
        });

        return this;
    }
});

OncoprintView = Backbone.View.extend({
    template: _.template($("#oncoprint_tmpl").html()),
    render: function() {
        var studyData = this.model;
        $(this.el).append(this.template(studyData));

        // ** Oncoprint
        var geneDataQuery = {
            genes: geneList,
            samples: studyData.case_set,
            geneticProfileIds: studyData.genetic_profiles,
            z_score_threshold: 2.0,
            rppa_score_threshold: 2.0
        };

        var oncoprint;
        $.post(DataManagerFactory.getGeneDataJsonUrl(), geneDataQuery, function(data) {
            var opElId = '#oncoprint_body_' + studyData.id;
            $(opElId + ' .loader-img').hide();

            var oncoPrintParams = {
                cancer_study_id: studyData.id,
                case_set_str: studyData.case_set_description,
                num_cases_affected: "0",
                percent_cases_affected: "0%",
                vis_key: false,
                customize: false
            };

            oncoPrintParams['data'] = data;

            oncoprint = Oncoprint($(opElId)[0], oncoPrintParams);
            oncoprint.draw();
        });
        // ** EOO

        return this;
    }
});

OncoPrintKeyView = Backbone.View.extend({
    template: _.template($("#oncoprint_key_tmpl").html()),
    render: function() {
        $(this.el).append(this.template());
        return this;
    }
});

/* Actual instances and applications */
$(document).ready(function() {
    new AppRouter();
    Backbone.history.start();
});