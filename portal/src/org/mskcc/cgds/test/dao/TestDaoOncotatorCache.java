package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoOncotatorCache;
import org.mskcc.cgds.scripts.ResetDatabase;
import java.io.IOException;

/**
 * JUnit Tests for DaoOncotator.
 *
 * @author Ethan Cerami.
 */
public class TestDaoOncotatorCache extends TestCase {

    public void testDaoOncotator() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        DaoOncotatorCache.addOncotatorRecord("abcde", "hello, world!");
        String json = DaoOncotatorCache.getOncotatorRecord("abcde");
        assertEquals("hello, world!", json);
    }
}