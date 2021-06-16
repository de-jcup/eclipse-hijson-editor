package de.jcup.hijson.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import de.jcup.hijson.outline.Item;

public class HighspeedJSONModel {
    
    private List<HighspeedJSONError> errors;
    private JsonNode rootNode;
    private Item rootItem;
    private Map<Integer,Item> itemOffsetMap;
	
	public HighspeedJSONModel() {
		errors = new ArrayList<>();
		itemOffsetMap=new HashMap<Integer, Item>();
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

    public void setRootItem(Item item) {
        this.rootItem=item;
    }
    public Item getRootItem() {
        return rootItem;
    }
    
    public Map<Integer, Item> getItemOffsetMap() {
        return itemOffsetMap;
    }
    
}
