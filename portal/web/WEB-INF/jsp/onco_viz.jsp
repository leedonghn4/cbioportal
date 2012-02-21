<html>
<head>
    <script src="js/jquery.min.js"></script>
    <script src="js/raphael/raphael-min.js" type="text/javascript" charset="utf-8"></script>
    <script src="js/onco_viz.js" type="text/javascript" charset="utf-8"></script>
</head>

<body>
<h2>OncoViz</h2>

<%
    String geneSymbol = request.getParameter("hugoGeneSymbol");
    String json = request.getParameter("json");
%>


Mutation Diagram for <%= geneSymbol %>:

<script>
    $(document).ready(function() {
        drawMutationDiagram(<%= json %>)
    });
</script>

<div id='mutation_diagram_<%= geneSymbol %>'></div>
<div id='mutation_diagram_details_<%= geneSymbol %>'>
    Roll-over in the diagram above to view details.
</div>

</body>
</html>