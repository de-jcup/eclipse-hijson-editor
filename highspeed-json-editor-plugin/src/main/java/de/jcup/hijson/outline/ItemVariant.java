package de.jcup.hijson.outline;

public enum ItemVariant {

    ARRAY("Array"),
    
    OBJECT("Object"),
    
    VALUE("Value"), 
    
    ARRAY_SEGMENT("Array-Segment"),
    
    ;
    
    private String text;

    private ItemVariant(String text){
        this.text=text;
    }
    
    public String getText() {
        return text;
    }
}
