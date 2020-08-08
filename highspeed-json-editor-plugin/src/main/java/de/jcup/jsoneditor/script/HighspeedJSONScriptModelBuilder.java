package de.jcup.jsoneditor.script;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.jcup.jsoneditor.document.JSONFormatSupport;

public class HighspeedJSONScriptModelBuilder {
    public HighspeedJSONScriptModel build(String text) {
        return build(text,false);
    }
    
	public HighspeedJSONScriptModel build(String text, boolean ignoreFailures) {
		HighspeedJSONScriptModel model = new HighspeedJSONScriptModel();
		
		
		if (text == null || text.trim().length() == 0) {
			return model;
		}
		try {
		    JsonNode rootNode = JSONFormatSupport.DEFAULT.read(text);
		    model.setRootNode(rootNode);
		    
		} catch (JsonProcessingException e) {
		    JsonLocation location = e.getLocation();
		    
		    HighspeedJSONError error = new HighspeedJSONError();
		    error.message=e.getMessage();
            model.getErrors().add(error);
		    
		    error.column = location.getColumnNr();
		    error.line = location.getLineNr();
		    error.offset = location.getCharOffset();
		}
		
		return model;
	}


}
