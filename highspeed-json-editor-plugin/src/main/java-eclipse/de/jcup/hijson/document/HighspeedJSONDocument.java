package de.jcup.hijson.document;

import org.eclipse.jface.text.Document;

import de.jcup.hijson.HighspeedJSONEditor;
import de.jcup.hijson.document.JSONFormatSupport;
import de.jcup.hijson.document.JSONFormatSupport.FormatterResult;

public class HighspeedJSONDocument extends Document {

    private HighspeedJSONEditor editor;

    public HighspeedJSONDocument(HighspeedJSONEditor editor) {
        this.editor=editor;
    }

    public void set(String text, long modificationStamp) {
        FormatterResult result = JSONFormatSupport.DEFAULT.formatJSONIfNotHavingMinAmountOfNewLines(text);
        if (result.state.hasContentChanged()) {
            text = result.getFormatted();
        }
        super.set(text, modificationStamp);
        if (result.state.hasContentChanged()) {
            editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling();
        }
    }
    
    public void setFormatted(String text) {
        super.set(text, getModificationStamp());
    }

}
