package de.jcup.jsoneditor.document;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import de.jcup.jsoneditor.HighspeedJSONEditor;
import de.jcup.jsoneditor.document.JSONFormatSupport.FormatterResult;

public class HighspeedJSONPartitionerAndAutoFormatSupport {
    
    public static final HighspeedJSONPartitionerAndAutoFormatSupport DEFAULT = new HighspeedJSONPartitionerAndAutoFormatSupport();

    public void setPartitionerAndFormatIfNecessary(HighspeedJSONEditor editor, IDocument document) {
        FormatterResult result = JSONFormatSupport.DEFAULT.formatJSONIfNotHavingMinAmountOfNewLines(document);
        /* installation necessary */
        
        IDocumentPartitioner partitioner = null;
        
        switch (result.state) {
        case FORMAT_DONE:
            /* document was a "one liner" and was transformed to multi lines - so rendered now fast by eclipse */
            editor.setErrorMessage("Auto format done - was necessary because one liners in eclipse would be extreme slow!");
            editor.markAsDirty();
            break;
        case KEPT_AS_IS:
            /* no changes necessary */
            break;
        case NOT_VALID_JSON_BUT_FALLBACK_DONE:
            editor.markAsDirty();
            break;
        case NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS:
            // we cannot handle this correctly - so we turn off syntax highlighting by using fallback partitioner
            partitioner=FallbackHighspeedJSONPartionerFactory.create();
            break;
        default:
            break;
        
        }
        if (partitioner==null) {
            partitioner=HighspeedJSONPartionerFactory.create();
        }
        
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }
    
}
