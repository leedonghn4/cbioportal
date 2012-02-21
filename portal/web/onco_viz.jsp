<html>

<body>

<h2>OncoViz</h2>
<form action="onco_viz.json" METHOD="POST">

    Enter a gene:
    <input type="text" name="hugoGeneSymbol" value="EGFR">

    <P>Paste Tab-Delim Mutations (colors are optional):
    <BR>
    <textarea name="mutations" rows="20" cols="60">
TCGA-1234 A289V RED
TCGA-1235 A289V RED
TCGA-1236 A289V RED
TCGA-1236 A289V BLUE
TCGA-1237 A289V BLUE
TCGA-1238 A289V ORANGE
    </textarea>
    <P>
    <input type="submit">
</form>

</body>
</html>