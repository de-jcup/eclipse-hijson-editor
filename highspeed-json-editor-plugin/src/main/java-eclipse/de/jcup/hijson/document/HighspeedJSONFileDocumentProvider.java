/*
 * Copyright 2020 Albert Tregnaghi
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import de.jcup.hijson.HighspeedJSONEditor;

/**
 * Document provider for files inside workspace
 * @author albert
 *
 */
public class HighspeedJSONFileDocumentProvider extends FileDocumentProvider {
    
    private HighspeedJSONEditor editor;

    public HighspeedJSONFileDocumentProvider(HighspeedJSONEditor editor){
        this.editor=editor;
    }
	
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		HighspeedJSONPartitionerAndAutoFormatSupport.DEFAULT.setPartitionerAndFormatIfNecessary(editor, document);
		return document;
	}
	
	@Override
	protected IDocument createEmptyDocument() {
	    return new HighspeedJSONDocument(editor);
	}
	
}