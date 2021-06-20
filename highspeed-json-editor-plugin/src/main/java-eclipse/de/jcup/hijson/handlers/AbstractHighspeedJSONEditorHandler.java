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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.jcup.hijson.HighspeedJSONEditor;

public abstract class AbstractHighspeedJSONEditorHandler extends AbstractHandler {

    public AbstractHighspeedJSONEditorHandler() {
        super();
    }

    /**
     * Execute something by using gradle editor instance
     * 
     * @param jsonEditor - never <code>null</code>
     */
    protected abstract void executeOnHighspeedJSONEditor(HighspeedJSONEditor jsonEditor);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return null;
        }
        IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return null;
        }
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null) {
            return null;
        }
        IEditorPart editor = activePage.getActiveEditor();

        if (editor instanceof HighspeedJSONEditor) {
            executeOnHighspeedJSONEditor((HighspeedJSONEditor) editor);
        }
        return null;
    }

}