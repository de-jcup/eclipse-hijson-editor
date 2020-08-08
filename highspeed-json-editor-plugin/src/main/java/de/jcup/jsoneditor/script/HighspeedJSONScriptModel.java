package de.jcup.jsoneditor.script;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class HighspeedJSONScriptModel {

    private List<HighspeedJSONError> errors;
    private JsonNode rootNode;
	
	public HighspeedJSONScriptModel() {
		errors = new ArrayList<>();
	}
	
	public JsonNode getRootNode() {
        return rootNode;
    }
	
	public List<HighspeedJSONError> getErrors() {
	    return errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

    public void setRootNode(JsonNode rootNode) {
        this.rootNode=rootNode;
    }

}
