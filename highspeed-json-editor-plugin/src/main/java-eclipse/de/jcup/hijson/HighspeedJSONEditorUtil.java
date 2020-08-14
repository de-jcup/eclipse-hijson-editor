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
package de.jcup.hijson;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import de.jcup.hijson.document.JSONFormatSupport;
import de.jcup.hijson.preferences.HighspeedJSONEditorPreferences;
import de.jcup.hijson.script.HighspeedJSONError;

public class HighspeedJSONEditorUtil {

    public static HighspeedJSONEditorPreferences getPreferences() {
        return HighspeedJSONEditorPreferences.getInstance();
    }

    private static UnpersistedMarkerHelper errorMarkerHelper = new UnpersistedMarkerHelper("de.jcup.hijson.error.marker");
    private static UnpersistedMarkerHelper infoMarkerHelper = new UnpersistedMarkerHelper("de.jcup.hijson.info.marker");

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
        infoMarkerHelper.removeMarkers(editorResource);

    }

    public static void addErrorMarker(IEditorPart editor, int line, HighspeedJSONError error) {
        if (editor == null) {
            return;
        }
        if (error == null) {
            return;
        }

        IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return;
        }
        IResource editorResource = input.getAdapter(IResource.class);
        addErrorMarker(line, error, editorResource);

    }

    public static void addErrorMarker(int line, HighspeedJSONError error, IResource editorResource) {
        if (editorResource == null) {
            return;
        }
        if (error == null) {
            return;
        }
        try {
            errorMarkerHelper.createErrorMarker(editorResource, error.message, line);
        } catch (CoreException e) {
            logError("Was not able to add error markers", e);
        }
    }

    public static void addInfoMarker(IEditorPart editor, int line, String message) {
        if (editor == null) {
            return;
        }
        if (message == null) {
            return;
        }

        IEditorInput input = editor.getEditorInput();
        if (input == null) {
            return;
        }
        IResource editorResource = input.getAdapter(IResource.class);
        addInfoMarker(line, message, editorResource);

    }

    public static void addInfoMarker(int line, String message, IResource editorResource) {
        if (editorResource == null) {
            return;
        }
        if (message == null) {
            return;
        }
        try {
            infoMarkerHelper.createMarker(editorResource, message, line, IMarker.SEVERITY_INFO, -1, -1);
        } catch (CoreException e) {
            logError("Was not able to add error markers", e);
        }
    }
    
    public static void addErrorMarker(int line, String message, IEditorInput input, int start, int end) {
        if (input == null) {
            return;
        }
        IResource editorResource = input.getAdapter(IResource.class);
        if (editorResource == null) {
            return;
        }
        if (message == null) {
            return;
        }
        try {
            errorMarkerHelper.createMarker(editorResource, message, line, IMarker.SEVERITY_ERROR, start, end);
        } catch (CoreException e) {
            logError("Was not able to add error markers", e);
        }
    }

    private static ILog getLog() {
        ILog log = HighspeedJSONEditorActivator.getDefault().getLog();
        return log;
    }

    public static void refreshAllowCommentsState() {
        JSONFormatSupport.DEFAULT.setAllowComents(HighspeedJSONEditorPreferences.getInstance().isAllowingComments());
    }

}
