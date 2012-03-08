package org.mskcc.portal.mut_diagram.servlet;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.mut_diagram.*;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorService;
import org.mskcc.portal.mut_diagram.oncotator.OncotatorRecord;
import org.mskcc.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.portal.mut_diagram.impl.CgdsIdMappingService;
import org.mskcc.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

/**
 * Mutation diagram data servlet.
 */
public final class OncoVizDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(OncoVizDataServlet.class);
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;
    private static final List<Sequence> EMPTY = Collections.emptyList();

    private final ObjectMapper objectMapper;
    private final FeatureService featureService;
    private final IdMappingService idMappingService;

    public OncoVizDataServlet() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);

        try {
            idMappingService = new CgdsIdMappingService(DaoGeneOptimized.getInstance());
        }
        catch (DaoException e) {
            throw new RuntimeException("could not create id mapping service", e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) 
            throws ServletException, IOException {
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");

        HashMap<String, String> proteinColorMap = extractProteinColorMap(request);

        List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);
        if (uniProtIds.isEmpty()) {
            writeSequencesToResponse(null, hugoGeneSymbol, EMPTY, proteinColorMap, response);
            return;
        }

        String uniProtId = uniProtIds.get(0);
        List<Sequence> sequences = featureService.getFeatures(uniProtId);
        if (sequences.isEmpty()) {
            writeSequencesToResponse(null, hugoGeneSymbol, EMPTY, proteinColorMap, response);
            return;
        }

        Sequence sequence = sequences.get(0);
        if (sequence.getMetadata() == null) {
            Map<String, Object> metadata = newHashMap();
            sequence.setMetadata(metadata);
        }
        sequence.getMetadata().put("hugoGeneSymbol", hugoGeneSymbol);
        sequence.getMetadata().put("uniProtId", uniProtId);

        List<ExtendedMutation> mutations = null;
        try {
            mutations = readMutations(proteinColorMap, request.getParameter("mutations"));
        } catch (DaoException e) {
            throw new IOException (e);
        }

        List<Markup> markups = createMarkups(mutations);
        writeSequencesToResponse(mutations, hugoGeneSymbol, ImmutableList.of(sequence.withMarkups(markups)),
                proteinColorMap, response);
    }

    private HashMap<String, String> extractProteinColorMap(HttpServletRequest request) {
        HashMap<String, String> proteinColorMap = new HashMap<String, String>();
        String textArea = request.getParameter("protein_color_map");
        logger.warn("text:  " + textArea);
        if (textArea != null) {
            String lines[] = textArea.split("\n");
            for (String line: lines) {
                if (line.trim().length()>0 && !line.startsWith("#")) {
                    String parts[] = line.split("\\s+");
                    logger.warn("Adding:  " + parts[0].trim() + " : " + parts[1].trim());
                    proteinColorMap.put(parts[0], parts[1]);
                }
            }
        }
        return proteinColorMap;
    }

    /**
     * Creats Markups From Specified Mutations.
     */
    private List<Markup> createMarkups(List<ExtendedMutation> mutations) {
        List<Markup> markups = newArrayList();
        HashMap<Integer, Integer> locationMap = new HashMap<Integer, Integer>();
        for (ExtendedMutation mutation: mutations) {
            String aaChange = mutation.getOncotatorRecord().getProteinChange();
            try {
                aaChange = aaChange.replace("_splice", "");
                int location = Integer.valueOf(aaChange.replaceAll("[A-Za-z\\.*]+", ""));
                int yoffset = 1;
                if (locationMap.containsKey(location)) {
                    yoffset = locationMap.get(location) + 1;
                }
                locationMap.put(location, yoffset);
                Markup markup = new Markup();
                markup.setDisplay("true");
                markup.setStart(location);
                markup.setEnd(location);
                markup.setColour(ImmutableList.of(mutation.getColor()));
                markup.setLineColour("#babdb6");
                markup.setHeadStyle("diamond");
                markup.setYoffset(yoffset);

                markup.setV_align("top");
                markup.setType("mutation");
                markup.setMetadata(new HashMap<String, Object>());
                markup.getMetadata().put("label", mutation.getOncotatorRecord().getProteinChange());
                markups.add(markup);
            }
            catch (NumberFormatException e) {
                logger.warn("ignoring extended mutation " + aaChange + ", no location information");
            }
        }
        return markups;
    }

    private void writeSequencesToResponse(List<ExtendedMutation> mutationList, String geneSymbol,
              final List<Sequence> sequences, HashMap<String, String> proteinColorMap, final HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.write("<html>");
        writer.write("<head>\n" +
                "<link href=\"css/global_portal.css\" type=\"text/css\" rel=\"stylesheet\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "\n");
        writer.write("<body>");
        writer.write("<div align=\"left\">");
        writer.write("<h2>OncoViz</h2>");
        writer.write("Almost there...<P>");
        writer.write("<form action='onco_viz.do' method='POST'>");
        writer.write("<input type='hidden' name='hugoGeneSymbol' value='"
            + geneSymbol + "'>");
        writer.write("<textarea name=\"json\" rows=\"20\" cols=\"60\">");
        StringWriter strWriter = new StringWriter();
        objectMapper.writeValue(strWriter, sequences);
        writer.write(strWriter.toString());
        writer.write("</textarea>");
        writer.write("<P><input type=\"submit\">");
        writer.write("</form>");

        if (mutationList != null) {
            writer.write("<table>");
            writer.write("<tr><th>Case ID</th><th>Genomic Change</th><th>Exon Affected</th><th>Protein Change</th>" +
                    "<th>COSMIC</th><th>Key</th></tr>");
            for (ExtendedMutation mutation:  mutationList) {
                writer.write("<tr><td>" + mutation.getCaseId() + "</td>");
                writer.write("<td>" + mutation.getOncotatorRecord().getGenomeChange() + "</td>");
                writer.write("<td>" + mutation.getOncotatorRecord().getExonAffected() + "</td>");
                writer.write("<td>" + mutation.getOncotatorRecord().getProteinChange() + "</td>");
                writer.write("<td>" + mutation.getOncotatorRecord().getCosmicOverlappingMutations() + "</td>");
                writer.write("<td><a href='http://jsonviewer.stack.hu/#http://www.broadinstitute.org/oncotator/mutation/" + mutation.getOncotatorRecord().getKey() + "'>OncotatorRecord JSON</a></td>");
                writer.write("</tr>");
            }
            writer.write("</table>");
        }
        writer.write("</div>");
        writer.write("</body>");
        writer.write("</html>");
    }

    /**
     * Reads in mutations from the Request Object.
     */
    List<ExtendedMutation> readMutations(HashMap<String, String> proteinColorMap,
            final String value) throws IOException, DaoException {
        OncotatorService service = OncotatorService.getInstance();
        List<ExtendedMutation> mutations = new ArrayList<ExtendedMutation>();
        if (value != null) {
            String lines[] = value.split("\n");
            for (String line: lines) {
                if (line.trim().length()>0) {
                    //  Split by any white space
                    String parts[] = line.split("\\s+");

                    //  Example:  TCGA-1234 7 140453136 140453136 A T RED
                    String caseId = parts[0];
                    String chr = parts[1];
                    String start = parts[2];
                    String end = parts[3];
                    String refAllele = parts[4];
                    String observedAllele = parts[5];
                    String color = "BLACK";
                    try {
                        color = parts[6];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    ExtendedMutation mutation = new ExtendedMutation();
                    mutation.setChr(chr);
                    mutation.setStartPosition(Long.parseLong(start));
                    mutation.setEndPosition(Long.parseLong(end));
                    mutation.setRefAllele(refAllele);
                    mutation.setObservedAllele(observedAllele);
                    mutation.setCaseId(caseId);
                    mutation.setColor(color);

                    //  Add OncotatorRecord Annotation
                    OncotatorRecord annotation = service.getOncotatorAnnotation(mutation.getChr(), mutation.getStartPosition(),
                            mutation.getEndPosition(), mutation.getRefAllele(), mutation.getObservedAllele());
                    mutation.setOncotatorRecord(annotation);

                    String proteinChange = annotation.getProteinChange();
                    if (proteinColorMap.containsKey(proteinChange)) {
                        mutation.setColor(proteinColorMap.get(proteinChange));
                    }

                    mutations.add(mutation);
                }
            }
        }
        return mutations;
    }
}
