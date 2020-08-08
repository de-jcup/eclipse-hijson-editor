/*
 * Copyright 2017 Albert Tregnaghi
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
package de.jcup.jsoneditor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import de.jcup.jsoneditor.preferences.HighspeedJSONEditorPreferences;
import de.jcup.jsoneditor.script.HighspeedJSONError;

public class HighspeedJSONEditorUtil {

    public static HighspeedJSONEditorPreferences getPreferences() {
        return HighspeedJSONEditorPreferences.getInstance();
    }

    private static UnpersistedMarkerHelper errorMarkerHelper = new UnpersistedMarkerHelper("de.jcup.jsoneditor.error.marker");

    public static void logInfo(String info) {
        getLog().log(new Status(IStatus.INFO, HighspeedJSONEditorActivator.PLUGIN_ID, info));
    }

    public static void logWarning(String warning) {
        getLog().log(new Status(IStatus.WARNING, HighspeedJSONEditorActivator.PLUGIN_ID, warning));
    }

    public static void logError(String error, Throwable t) {
        getLog().log(new Status(IStatus.ERROR, HighspeedJSONEditorActivator.PLUGIN_ID, error, t));
    }

    public static void removeScriptErrors(IEditorPart editor) {
        if (editor == null) {
            return;
        }
        IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return;
        }
        IResource editorResource = input.getAdapter(IResource.class);
        errorMarkerHelper.removeMarkers(editorResource);

    }

    public static void addErrorMarker(IEditorPart editor, int line, HighspeedJSONError marker) {
        if (editor == null) {
            return;
        }
        if (marker == null) {
            return;
        }

        IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return;
        }
        IResource editorResource = input.getAdapter(IResource.class);
        addErrorMarker(line, marker, editorResource);

    }


    public static void addErrorMarker(int line, HighspeedJSONError marker, IResource editorResource) {
        if (editorResource == null) {
            return;
        }
        if (marker == null) {
            return;
        }
        try {
            errorMarkerHelper.createErrorMarker(editorResource, marker.message, line);
        } catch (CoreException e) {
            logError("Was not able to add error markers", e);
        }
    }

    private static ILog getLog() {
        ILog log = HighspeedJSONEditorActivator.getDefault().getLog();
        return log;
    }

}
