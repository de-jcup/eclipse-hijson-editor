package de.jcup.hijson.document;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import de.jcup.hijson.HighspeedJSONEditor;
import de.jcup.hijson.document.JSONFormatSupport;
import de.jcup.hijson.document.JSONFormatSupport.FormatterResult;

public class HighspeedJSONPartitionerAndAutoFormatSupport {
    
    public static final HighspeedJSONPartitionerAndAutoFormatSupport DEFAULT = new HighspeedJSONPartitionerAndAutoFormatSupport();

    public void setPartitionerAndFormatIfNecessary(HighspeedJSONEditor editor, IDocument document) {
        FormatterResult result = JSONFormatSupport.DEFAULT.formatJSONIfNotHavingMinAmountOfNewLines(document.get());
        /* installation necessary */
        
        IDocumentPartitioner partitioner = null;
        
        switch (result.state) {
        case FORMAT_DONE:
            document.set(result.getFormatted());
            editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling();
            break;
        case KEPT_AS_IS:
            /* no changes necessary */
            break;
        case NOT_VALID_JSON_BUT_FALLBACK_DONE:
            document.set(result.getFormatted());
            editor.markDirtyBecauseFormatWasNecessaryForOneLinerHandling();
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
