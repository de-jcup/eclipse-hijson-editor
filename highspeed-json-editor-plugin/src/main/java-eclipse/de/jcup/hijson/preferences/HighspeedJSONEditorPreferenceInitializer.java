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
 


import static de.jcup.hijson.HighspeedJSONEditorColorConstants.*;
import static de.jcup.hijson.HighspeedJSONEditorUtil.*;
import static de.jcup.hijson.preferences.HighspeedJSONEditorPreferenceConstants.*;
import static de.jcup.hijson.preferences.HighspeedJSONEditorSyntaxColorPreferenceConstants.*;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class HighspeedJSONEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		HighspeedJSONEditorPreferences preferences = getPreferences();
		IPreferenceStore store = preferences.getPreferenceStore();
		
		/* Outline */
		store.setDefault(P_LINK_OUTLINE_WITH_EDITOR.getId(), false);
		store.setDefault(P_CREATE_OUTLINE_FOR_NEW_EDITOR.getId(), false); // per default no outline created - increases speed, reduces memory consumption
		/* PARSING */
		store.setDefault(P_EDITOR_ALLOW_COMMENTS_ENABLED.getId(), true);
		store.setDefault(P_EDITOR_ALLOW_UNQUOTED_CONTROL_CHARS.getId(), false);
		/* VALIDATION*/
		store.setDefault(P_VALIDATE_ON_SAVE.getId(), true);
		
		/* ++++++++++++ */
		/* + Brackets + */
		/* ++++++++++++ */
		/* bracket rendering configuration */
		store.setDefault(P_EDITOR_MATCHING_BRACKETS_ENABLED.getId(), true); // per default matching is enabled, but without the two other special parts
		store.setDefault(P_EDITOR_HIGHLIGHT_BRACKET_AT_CARET_LOCATION.getId(), false);
		store.setDefault(P_EDITOR_ENCLOSING_BRACKETS.getId(), false);
		store.setDefault(P_EDITOR_AUTO_CREATE_END_BRACKETSY.getId(), true);
		

        /* +++++++++++++++++ */
        /* + Editor Colors + */
        /* +++++++++++++++++ */
        preferences.setDefaultColor(COLOR_NORMAL_TEXT, BLACK);
        preferences.setDefaultColor(COLOR_COMMENT, GREEN_JAVA);
        preferences.setDefaultColor(COLOR_STRING, DARK_BLUE);
        preferences.setDefaultColor(COLOR_NULL, KEYWORD_DEFAULT_PURPLE);
		
		/* bracket color */
		preferences.setDefaultColor(P_EDITOR_MATCHING_BRACKETS_COLOR, GRAY_JAVA);
		preferences.setDefaultColor(HighspeedJSONEditorSyntaxColorPreferenceConstants.COLOR_NULL, GRAY_JAVA);
		
		/* +++++++++++++++++++ */
		/* + Code Assistence + */
		/* +++++++++++++++++++ */
		store.setDefault(P_CODE_ASSIST_ADD_KEYWORDS.getId(), true);
		store.setDefault(P_CODE_ASSIST_ADD_SIMPLEWORDS.getId(), true);
		
	}
	
	
	
}
