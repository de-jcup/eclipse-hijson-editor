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
package de.jcup.hijson.handlers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import de.jcup.hijson.HighspeedJSONEditor;

public class CopyJsonAsOneLineToClipboardHandler extends AbstractHighspeedJSONEditorHandler {

    @Override
    protected void executeOnHighspeedJSONEditor(HighspeedJSONEditor jsonEditor) {
        String reducedJson = jsonEditor.createJsonAsOneLine();
        
        StringSelection selection = new StringSelection(reducedJson);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

}
