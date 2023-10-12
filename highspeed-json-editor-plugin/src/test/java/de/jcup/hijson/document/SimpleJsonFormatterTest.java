package de.jcup.hijson.document;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimpleJsonFormatterTest {

    private SimpleJsonFormatter formatterToTest;

    @Before
    public void before() {
        formatterToTest = new SimpleJsonFormatter();
    }
    
    @Test
    public void formatJson_reduces_lines() {
        /* prepare */
        /* @formatter:off */
        String json = "{ // comment 0 is here\n" + 
                "                    \n" + 
                "                    // comment 1 is here\n" + 
                "                    \n" + 
                "                    \"a\" : \"value1\",\n" + 
                "                    \n" + 
                "                    \n" + 
                "                    \"b\" : \"value2\", // comment2 is here\n" + 
                "                    \"c\" : \"value3\"\n" + 
                "                        \n" + 
                "                    \n" + 
                "            }";
                
            
        /* execute */
        String formatted = formatterToTest.formatJson(json);

        /* test */
        String expected = "{ // comment 0 is here\n" + 
                "                    // comment 1 is here\n" + 
                "                    \"a\" : \"value1\",\n" + 
                "                    \"b\" : \"value2\", // comment2 is here\n" + 
                "                    \"c\" : \"value3\"\n" + 
                "            }";
        /* @formatter:on */
        
        assertEquals(expected,formatted );
    }

}
