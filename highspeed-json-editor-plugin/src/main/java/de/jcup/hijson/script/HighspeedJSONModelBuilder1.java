package de.jcup.hijson.script;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import de.jcup.hijson.document.JSONFormatSupport;

@Deprecated // use HighspeedJSONModelBuilder2 instead
public class HighspeedJSONModelBuilder1 implements HighSpeedJSONModelBuilder {
    @Override
    public HighspeedJSONModel build(String text) {
        return build(text,false);
    }
    
	@Override
    public HighspeedJSONModel build(String text, boolean ignoreFailures) {
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
		    error.message=e.getMessage();
            model.getErrors().add(error);
		    
		    error.column = location.getColumnNr();
		    error.line = location.getLineNr();
		    error.offset = location.getCharOffset();
		}
		
		return model;
	}


}
