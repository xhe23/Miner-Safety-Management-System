package edu.uic.cs442.msms;


import org.junit.Test;
import android.util.Log;

import edu.uic.cs442.msms.manager.DataManager;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void data_manager() throws Exception {

        DataManager dataManager = DataManager.getInstance();

        assertNotNull(dataManager);

    }
}