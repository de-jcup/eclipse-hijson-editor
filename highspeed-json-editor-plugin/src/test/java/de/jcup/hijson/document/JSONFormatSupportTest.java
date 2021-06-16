package de.jcup.hijson.document;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.jcup.hijson.document.JSONFormatSupport.FormatterResult;
import de.jcup.hijson.test.util.TextFileReader;

public class JSONFormatSupportTest {

    private JSONFormatSupport supportToTest;
    private TextFileReader textFileReader;

    @Before
    public void beforeAll() {
        textFileReader = new TextFileReader();

        supportToTest = new JSONFormatSupport();
        
    }
    
    @Test
    public void bugfix_11_array_out_of_bounds() {
        String json = textFileReader.loadBugifxTextFile("bugfix-11-array-index-out-of-bounds.json");
        
        /* execute */
        FormatterResult result = supportToTest.formatJSON(json);
        
        /* test */
        assertNotNull(result);
    }
    

}
