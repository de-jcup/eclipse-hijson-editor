/*
 * Copyright 2021 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
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
    
    @Test
    public void createJSONAsOneLine() {
        /* prepare */
        String json = "{\n      \"name\" :\"alberts\\n  test\", \n    \"message\" :  \"test\"\n"
                + "   }  \n";
        
        /* execute */
        String oneLine = supportToTest.createJSONAsOneLine(json);
        
        /* test*/
        assertEquals("{ \"name\" : \"alberts\\n  test\", \"message\" : \"test\" }", oneLine);
    }

}
