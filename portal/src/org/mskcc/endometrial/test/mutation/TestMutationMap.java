package org.mskcc.endometrial.test.mutation;

import junit.framework.TestCase;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.endometrial.mutation.MutationMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Tests the MutationMap.
 */
public class TestMutationMap extends TestCase {

    /**
     * Tests the MutationMap on a small sample MAF File.
     *
     * @throws IOException IO Error.
     */
    public void testMutationMap() throws IOException {
        HashSet<String> targetGeneSet = new HashSet<String>();
        targetGeneSet.add("A1BG");
        targetGeneSet.add("A2M");
        File mafFile = new File("test_data/endo_maf_test.txt");
        MutationMap mutationMap = new MutationMap(mafFile, targetGeneSet);

        //  Verify that we get the correct number of mutation records back
        //  Note that the answer is not 3, because one of the mutations is silent,
        //  and mutation map (correctly) filters silent mutations.
        ArrayList<ExtendedMutation> mutationList = mutationMap.getMutations("A1BG", "TCGA-D1-A162");
        assertEquals(2, mutationList.size());

        //  Verify Mutation Record Details
        ExtendedMutation mutation = mutationList.get(0);
        assertEquals("19", mutation.getChr());
        assertEquals(58861760, mutation.getStart());
        assertEquals(58861760, mutation.getEnd());
        assertEquals("Missense_Mutation", mutation.getMutationType());
        assertEquals("C", mutation.getReferenceAllele());
        assertEquals("T", mutation.getTumorAllele());
    }
}
