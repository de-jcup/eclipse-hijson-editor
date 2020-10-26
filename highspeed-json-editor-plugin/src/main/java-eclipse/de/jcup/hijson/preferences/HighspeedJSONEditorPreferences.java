package de.jcup.hijson.preferences;
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

import static de.jcup.hijson.preferences.HighspeedJSONEditorPreferenceConstants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.jcup.hijson.ColorUtil;
import de.jcup.hijson.HighspeedJSONEditorActivator;

public class HighspeedJSONEditorPreferences {

	private static HighspeedJSONEditorPreferences INSTANCE = new HighspeedJSONEditorPreferences();
	private IPreferenceStore store;

	private HighspeedJSONEditorPreferences() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, HighspeedJSONEditorActivator.PLUGIN_ID);
	}

	public String getStringPreference(HighspeedJSONEditorPreferenceConstants id) {
		String data = getPreferenceStore().getString(id.getId());
		if (data == null) {
			data = "";
		}
		return data;
	}

	public boolean getBooleanPreference(HighspeedJSONEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getBoolean(id.getId());
		return data;
	}

	public void setBooleanPreference(HighspeedJSONEditorPreferenceConstants id, boolean value) {
		getPreferenceStore().setValue(id.getId(), value);
	}

	public boolean isLinkOutlineWithEditorEnabled() {
		return getBooleanPreference(P_LINK_OUTLINE_WITH_EDITOR);
	}
	
	public boolean isOutlineBuildEnabled() {
        return getBooleanPreference(P_CREATE_OUTLINE_FOR_NEW_EDITOR);
    }
	public boolean isValidateOnSaveEnabled() {
	    return getBooleanPreference(P_VALIDATE_ON_SAVE);
	}
	
	public boolean isAllowingComments() {
	    return getBooleanPreference(P_EDITOR_ALLOW_COMMENTS_ENABLED);
	}

	public boolean isAllowingUnquotedControlChars() {
	    return getBooleanPreference(P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS);
	}

	public IPreferenceStore getPreferenceStore() {
		return store;
	}

	public boolean getDefaultBooleanPreference(HighspeedJSONEditorPreferenceConstants id) {
		boolean data = getPreferenceStore().getDefaultBoolean(id.getId());
		return data;
	}

	public RGB getColor(PreferenceIdentifiable identifiable) {
		RGB color = PreferenceConverter.getColor(getPreferenceStore(), identifiable.getId());
		return color;
	}

	/**
	 * Returns color as a web color in format "#RRGGBB"
	 * 
	 * @param identifiable
	 * @return web color string
	 */
	public String getWebColor(PreferenceIdentifiable identifiable) {
		RGB color = getColor(identifiable);
		if (color == null) {
			return null;
		}
		String webColor = ColorUtil.convertToHexColor(color);
		return webColor;
	}

	public void setDefaultColor(PreferenceIdentifiable identifiable, RGB color) {
		PreferenceConverter.setDefault(getPreferenceStore(), identifiable.getId(), color);
	}

	public static HighspeedJSONEditorPreferences getInstance() {
		return INSTANCE;
	}

	

}
