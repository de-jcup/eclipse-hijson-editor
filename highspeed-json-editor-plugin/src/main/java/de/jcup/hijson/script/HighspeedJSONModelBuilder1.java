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
package de.jcup.hijson.script;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.jcup.hijson.document.JSONFormatSupport;

@Deprecated // use HighspeedJSONModelBuilder2 instead
public class HighspeedJSONModelBuilder1 implements HighSpeedJSONModelBuilder {
    @Override
    public HighspeedJSONModel build(String text, int tresholdGroupArrays) {
        return build(text, tresholdGroupArrays, false);
    }

    @Override
    public HighspeedJSONModel build(String text, int tresholdGroupArrays, boolean ignoreFailures) {
        HighspeedJSONModel model = new HighspeedJSONModel();

        if (text == null || text.trim().length() == 0) {
            return model;
        }
        try {
            JsonNode rootNode = JSONFormatSupport.DEFAULT.read(text);
            model.setRootNode(rootNode);

        } catch (JsonProcessingException e) {
            JsonLocation location = e.getLocation();

            HighspeedJSONError error = new HighspeedJSONError();
            error.message = e.getMessage();
            model.getErrors().add(error);

            error.column = location.getColumnNr();
            error.line = location.getLineNr();
            error.offset = location.getCharOffset();
        }

        return model;
    }

}
