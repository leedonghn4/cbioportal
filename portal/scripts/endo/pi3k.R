#!/usr/bin/Rscript --no-save
library(heatmap.plus)
library ("gplots")
options(warn=-1)

heatmap.plus2 <- function (x, Rowv = NULL, Colv = if (symm) "Rowv" else NULL, 
    distfun = dist, hclustfun = hclust, reorderfun = function(d, 
        w) reorder(d, w), add.expr, symm = FALSE, revC = identical(Colv, 
        "Rowv"), scale = c("row", "column", "none"), na.rm = TRUE, 
    margins = c(5, 5), ColSideColors, RowSideColors, cexRow = 0.2 + 
        1/log10(nr), cexCol = 0.2 + 1/log10(nc), labRow = NULL, 
    labCol = NULL, main = NULL, xlab = NULL, ylab = NULL, keep.dendro = FALSE, 
    verbose = getOption("verbose"), ...) 
{
    scale <- if (symm && missing(scale)) 
        "none"
    else match.arg(scale)
    if (length(di <- dim(x)) != 2 || !is.numeric(x)) 
        stop("'x' must be a numeric matrix")
    nr <- di[1]
    nc <- di[2]
    if (nr <= 1 || nc <= 1) 
        stop("'x' must have at least 2 rows and 2 columns")
    if (!is.numeric(margins) || length(margins) != 2) 
        stop("'margins' must be a numeric vector of length 2")
    doRdend <- !identical(Rowv, NA)
    doCdend <- !identical(Colv, NA)
    if (is.null(Rowv)) 
        Rowv <- rowMeans(x, na.rm = na.rm)
    if (is.null(Colv)) 
        Colv <- colMeans(x, na.rm = na.rm)
    if (doRdend) {
        if (inherits(Rowv, "dendrogram")) 
            ddr <- Rowv
        else {
            hcr <- hclustfun(distfun(x))
            ddr <- as.dendrogram(hcr)
            if (!is.logical(Rowv) || Rowv) 
                ddr <- reorderfun(ddr, Rowv)
        }
        if (nr != length(rowInd <- order.dendrogram(ddr))) 
            stop("row dendrogram ordering gave index of wrong length")
    }
    else rowInd <- 1:nr
    if (doCdend) {
        if (inherits(Colv, "dendrogram")) 
            ddc <- Colv
        else if (identical(Colv, "Rowv")) {
            if (nr != nc) 
                stop("Colv = \"Rowv\" but nrow(x) != ncol(x)")
            ddc <- ddr
        }
        else {
            hcc <- hclustfun(distfun(if (symm) 
                x
            else t(x)))
            ddc <- as.dendrogram(hcc)
            if (!is.logical(Colv) || Colv) 
                ddc <- reorderfun(ddc, Colv)
        }
        if (nc != length(colInd <- order.dendrogram(ddc))) 
            stop("column dendrogram ordering gave index of wrong length")
    }
    else colInd <- 1:nc
    x <- x[rowInd, colInd]
    labRow <- if (is.null(labRow)) 
        if (is.null(rownames(x))) 
            (1:nr)[rowInd]
        else rownames(x)
    else labRow[rowInd]
    labCol <- if (is.null(labCol)) 
        if (is.null(colnames(x))) 
            (1:nc)[colInd]
        else colnames(x)
    else labCol[colInd]
    if (scale == "row") {
        x <- sweep(x, 1, rowMeans(x, na.rm = na.rm))
        sx <- apply(x, 1, sd, na.rm = na.rm)
        x <- sweep(x, 1, sx, "/")
    }
    else if (scale == "column") {
        x <- sweep(x, 2, colMeans(x, na.rm = na.rm))
        sx <- apply(x, 2, sd, na.rm = na.rm)
        x <- sweep(x, 2, sx, "/")
    }
    lmat <- rbind(c(NA, 3), 2:1)
    lwid <- c(if (doRdend) 1 else 0.05, 4)
    lhei <- c((if (doCdend) 1 else 0.05) + if (!is.null(main)) 0.2 else 0, 
        4)
    if (!missing(ColSideColors)) {
        if (!is.matrix(ColSideColors)) 
            stop("'ColSideColors' must be a matrix")
        if (!is.character(ColSideColors) || dim(ColSideColors)[1] != 
            nc) 
            stop("'ColSideColors' dim()[2] must be of length ncol(x)")
        lmat <- rbind(lmat[1, ] + 1, c(NA, 1), lmat[2, ] + 1)
        lhei <- c(lhei[1], 0.2, lhei[2])
    }
    if (!missing(RowSideColors)) {
        if (!is.matrix(RowSideColors)) 
            stop("'RowSideColors' must be a matrix")
        if (!is.character(RowSideColors) || dim(RowSideColors)[1] != 
            nr) 
            stop("'RowSideColors' must be a character vector of length nrow(x)")
        lmat <- cbind(lmat[, 1] + 1, c(rep(NA, nrow(lmat) - 1), 
            1), lmat[, 2] + 1)
        lwid <- c(lwid[1], 0.2, lwid[2])
    }
    lmat[is.na(lmat)] <- 0
    if (verbose) {
        cat("layout: widths = ", lwid, ", heights = ", lhei, 
            "; lmat=\n")
        print(lmat)
    }
    op <- par(no.readonly = TRUE)
    on.exit(par(op))
    layout(lmat, widths = lwid, heights = lhei, respect = FALSE)
    if (!missing(RowSideColors)) {
        par(mar = c(margins[1], 0, 0, 0.5))
        rsc = RowSideColors[rowInd, ]
        rsc.colors = matrix()
        rsc.names = names(table(rsc))
        rsc.i = 1
        for (rsc.name in rsc.names) {
            rsc.colors[rsc.i] = rsc.name
            rsc[rsc == rsc.name] = rsc.i
            rsc.i = rsc.i + 1
        }
        rsc = matrix(as.numeric(rsc), nrow = dim(rsc)[1])
        image(t(rsc), col = as.vector(rsc.colors), axes = FALSE)
        if (length(colnames(RowSideColors)) > 0) {
            axis(1, 0:(dim(rsc)[2] - 1)/(dim(rsc)[2] - 1), colnames(RowSideColors), 
                las = 2, tick = FALSE)
        }
    }
    if (!missing(ColSideColors)) {
        par(mar = c(0.5, 0, 0, margins[2]))
        csc = ColSideColors[colInd, ]
        csc.colors = matrix()
        csc.names = names(table(csc))
        csc.i = 1
        for (csc.name in csc.names) {
            csc.colors[csc.i] = csc.name
            csc[csc == csc.name] = csc.i
            csc.i = csc.i + 1
        }
        csc = matrix(as.numeric(csc), nrow = dim(csc)[1])
        image(csc, col = as.vector(csc.colors), axes = FALSE)
        if (length(colnames(ColSideColors)) > 0) {
            axis(2, 0:(dim(csc)[2] - 1)/(dim(csc)[2] - 1), colnames(ColSideColors), 
                las = 2, tick = FALSE, cex.axis=.4)
        }
    }
    par(mar = c(margins[1], 0, 0, margins[2]))
    if (!symm || scale != "none") {
        x <- t(x)
    }
    if (revC) {
        iy <- nr:1
        ddr <- rev(ddr)
        x <- x[, iy]
    }
    else iy <- 1:nr
    image(1:nc, 1:nr, x, xlim = 0.5 + c(0, nc), ylim = 0.5 + 
        c(0, nr), axes = FALSE, xlab = "", ylab = "", ...)
    axis(1, 1:nc, labels = labCol, las = 2, line = -0.5, tick = 0, 
        cex.axis = cexCol)
    if (!is.null(xlab)) 
        mtext(xlab, side = 1, line = margins[1] - 1.25)
    axis(4, iy, labels = labRow, las = 2, line = -0.5, tick = 0, 
        cex.axis = cexRow)
    if (!is.null(ylab)) 
        mtext(ylab, side = 4, line = margins[2] - 1.25)
    if (!missing(add.expr)) 
        eval(substitute(add.expr))
    par(mar = c(margins[1], 0, 0, 0))
    if (doRdend) 
        plot(ddr, horiz = TRUE, axes = FALSE, yaxs = "i", leaflab = "none")
    else frame()
    par(mar = c(0, 0, if (!is.null(main)) 1 else 0, margins[2]))
    if (doCdend) 
        plot(ddc, axes = FALSE, xaxs = "i", leaflab = "none")
    else if (!is.null(main)) 
        frame()
    if (!is.null(main)) 
        title(main, cex.main = 1.5 * op[["cex.main"]])
    invisible(list(rowInd = rowInd, colInd = colInd, Rowv = if (keep.dendro && 
        doRdend) ddr, Colv = if (keep.dendro && doCdend) ddc))
}

oncoprint <- function (m1, m2, colors, title) {
  colnames(m1) = rep("", ncol(m1))
  #colnames(m2) = c("MSI", "Mutation Rate", "PTEN - Protein", "AKT_pS473", "AKT_pT308")
  colnames(m2) = c("MSI", "Mutation Rate", "PTEN - Protein", "AKT_pS473", "AKT_pT308")
  heatmap.plus2(m1, margins=c(5,15), cexRow=1.0, scale="none", col=colors, ColSideColors=m2)
}

oncoprints_all <- function(sub_df, title) {
  textplot (title, col="darkblue", valign="top", cex=2.0)
  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_0, PIK3CA_MUTATED_0, PIK3R1_MUTATED_0, 
      PIK3R2_MUTATED_0, AKT1_MUTATED_0, AKT2_MUTATED_0, AKT3_MUTATED_0))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 0"))

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_1, PIK3CA_MUTATED_1, PIK3R1_MUTATED_1, 
      PIK3R2_MUTATED_1, AKT1_MUTATED_1, AKT2_MUTATED_1, AKT3_MUTATED_1))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "pink", "red")
  oncoprint(m1, m2, colors, paste(title, "Mutation - Level 1")) 

  # the genes / events to focus on, extract the columns we want to place in the Heatmap
  events = subset(sub_df, select=c(PTEN_MUTATED_3, PIK3CA_MUTATED_3, PIK3R1_MUTATED_3, 
      PIK3R2_MUTATED_3, AKT1_MUTATED_3, AKT2_MUTATED_3, AKT3_MUTATED_3))
  colors = subset(sub_df, select=c(COL1, COL2, COL3, COL4, COL5))
  
  # Convert to Matrix and transpose
  m1 = t(as.matrix(events))
  m2 = as.matrix(colors)
  colors = c("#EEEEEE", "pink", "red")
  oncoprint(m1, m2, colors, paste(title, "Mut - Level 3")) 
}  


# Read in Unified Clinical File
df = read.delim("~/SugarSync/endo/data/out/ucec_clinical_with_clusters_unified.txt")

# Create new SUBTYPE Column that has Shorter Labels
df = transform(df, SUBTYPE="NA")
df$SUBTYPE = factor(df$SUBTYPE, levels = c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3", "Mixed", "Serous"))
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 1)",]$SUBTYPE="Endo-Grade-1"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 2)",]$SUBTYPE="Endo-Grade-2"
df[df$histological_typeCorrected=="Endometrioid endometrial adenocarcinoma (Grade 3)",]$SUBTYPE="Endo-Grade-3"
df[df$histological_typeCorrected=="Mixed serous and endometrioid",]$SUBTYPE="Mixed"
df[df$histological_typeCorrected=="Uterine serous endometrial adenocarcinoma",]$SUBTYPE="Serous"

# Create Color Mappings
df = transform(df, COL1="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL2="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL3="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL4="#FFFFFF", stringsAsFactors=FALSE)
df = transform(df, COL5="#FFFFFF", stringsAsFactors=FALSE)
msi_colors = c("#DDDDDD", "#CCFFCC", "#FFCCFF", "#FF0000")
cna_colors = c("#CCFFFF", "#33FFFF", "#0000FF")
rppa_colors = c("red", "green")
df[df$MSI_STATUS %in% "Indeterminant",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "Not Done",]$COL1=msi_colors[1]
df[df$MSI_STATUS %in% "MSS",]$COL1=msi_colors[2]
df[df$MSI_STATUS %in% "MSI-L",]$COL1=msi_colors[3]
df[df$MSI_STATUS %in% "MSI-H",]$COL1=msi_colors[4]
df[df$MUTATION_RATE_CLUSTER %in% "1_LOW",]$COL2=cna_colors[1]
df[df$MUTATION_RATE_CLUSTER %in% "2_HIGH",]$COL2=cna_colors[2]
df[df$MUTATION_RATE_CLUSTER %in% "3_HIGHEST",]$COL2=cna_colors[3]
df$COL3 <- ifelse(df$PTEN_PROTEIN_LEVEL <= 0, rppa_colors[1], df$COL3)
df$COL3 <- ifelse(df$PTEN_PROTEIN_LEVEL > 0, rppa_colors[2], df$COL3)
df$COL4 <- ifelse(df$AKT_pS473 <= 0, rppa_colors[1], df$COL4)
df$COL4 <- ifelse(df$AKT_pS473 > 0, rppa_colors[2], df$COL4)
df$COL5 <- ifelse(df$AKT_pT308 <= 0, rppa_colors[1], df$COL5)
df$COL5 <- ifelse(df$AKT_pT308 > 0, rppa_colors[2], df$COL5)

plot(density(df$PTEN_PROTEIN_LEVEL, na.rm=T))
hist(df$PTEN_PROTEIN_LEVEL, na.rm=T)

plot(density(df$AKT_pS473, na.rm=T))
hist(df$AKT_pS473, na.rm=T)

plot(density(df$AKT_pT308, na.rm=T))
hist(df$AKT_pT308, na.rm=T)

# Only Sequenced Cases
df = subset(df, SEQUENCED=="Y")

#######################################
# Endometriod Only
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
oncoprints_all(sub_df, "Endometriod")

#######################################
# Serous Only
sub_df = subset(df, SUBTYPE %in% c("Serous"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
oncoprints_all(sub_df, "Serous")

#######################################
# Endometriod:  MUT LOW
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "1_LOW")
oncoprints_all(sub_df, "Endometriod - Mut Low")

#######################################
# Endometriod:  MUT HIGH
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "2_HIGH")
oncoprints_all(sub_df, "Endometriod - Mut High")

#######################################
# Endometriod:  MUT HIGHEST
sub_df = subset(df, SUBTYPE %in% c("Endo-Grade-1", "Endo-Grade-2", "Endo-Grade-3"))
sub_df = subset(sub_df, MUTATION_RATE_CLUSTER %in% "3_HIGHEST")
oncoprints_all(sub_df, "Endometriod - Mut Highest")
