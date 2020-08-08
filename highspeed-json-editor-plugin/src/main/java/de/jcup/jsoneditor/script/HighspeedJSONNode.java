package de.jcup.jsoneditor.script;

public class HighspeedJSONNode {

	private String name;
	int pos;
	int end;

	public HighspeedJSONNode(String name){
		this.name=name;
	}
	
	public String getName() {
		return name;
	}

	public int getPosition() {
		return pos;
	}

	public int getLengthToNameEnd() {
		return name.length();
	}

	public int getEnd() {
		return end;
	}

}
