package de.jcup.hijson.handlers;

import de.jcup.hijson.HighspeedJSONEditor;

public class GotoMatchingBracketHandler extends AbstractHighspeedJSONEditorHandler {

    @Override
    protected void executeOnHighspeedJSONEditor(HighspeedJSONEditor jsonEditor) {
        jsonEditor.gotoMatchingBracket();
    }
}