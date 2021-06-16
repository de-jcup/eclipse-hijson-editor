/*
 * Copyright 2021 Albert Tregnaghi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 */
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
