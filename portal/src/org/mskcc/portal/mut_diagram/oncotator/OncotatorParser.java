package org.mskcc.portal.mut_diagram.oncotator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Parses JSON Retrieved from OncotatorRecord.
 */
public class OncotatorParser {
    
    public static OncotatorRecord parseJSON (String key, String json) throws IOException {
        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(json, JsonNode.class);

        OncotatorRecord oncotatorRecord = new OncotatorRecord(key);
        
        JsonNode genomeChange = rootNode.path("genome_change");
        if (genomeChange != null) {
            oncotatorRecord.setGenomeChange(genomeChange.getTextValue());
        }
        
        JsonNode cosmic = rootNode.path("Cosmic_overlapping_mutations");
        if (cosmic != null) {
            oncotatorRecord.setCosmicOverlappingMutations(cosmic.getTextValue());
        } else {
            oncotatorRecord.setCosmicOverlappingMutations("NO COSMIC DATA");
        }

        JsonNode bestTranscriptIndexNode = rootNode.path("best_canonical_transcript");

        if (bestTranscriptIndexNode != null) {
            int transcriptIndex = bestTranscriptIndexNode.getIntValue();
            JsonNode transcriptsNode = rootNode.path("transcripts");
            JsonNode bestTranscriptNode = transcriptsNode.get(transcriptIndex);

            String variantClassification = bestTranscriptNode.path("variant_classification").getTextValue();
            String proteinChange = bestTranscriptNode.path("protein_change").getTextValue();
            String geneSymbol = bestTranscriptNode.path("gene").getTextValue();
            int exonAffected = bestTranscriptNode.path("exon_affected").getIntValue();
            oncotatorRecord.setVariantClassification(variantClassification);
            oncotatorRecord.setProteinChange(proteinChange);
            oncotatorRecord.setGene(geneSymbol);
            oncotatorRecord.setExonAffected(exonAffected);
        }
        
        return oncotatorRecord;
    }
}
