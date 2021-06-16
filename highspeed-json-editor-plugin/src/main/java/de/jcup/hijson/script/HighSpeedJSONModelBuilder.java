package de.jcup.hijson.script;

public interface HighSpeedJSONModelBuilder {

    HighspeedJSONModel build(String text);

    HighspeedJSONModel build(String text, boolean ignoreFailures);

}