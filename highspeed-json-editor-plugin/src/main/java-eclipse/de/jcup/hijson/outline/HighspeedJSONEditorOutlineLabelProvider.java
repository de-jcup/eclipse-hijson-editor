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
package de.jcup.hijson.outline;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import de.jcup.hijson.ColorManager;
import de.jcup.hijson.EclipseUtil;
import de.jcup.hijson.HighspeedJSONEditorActivator;
import de.jcup.hijson.HighspeedJSONEditorColorConstants;
import de.jcup.hijson.SimpleStringUtils;

public class HighspeedJSONEditorOutlineLabelProvider extends BaseLabelProvider implements IStyledLabelProvider, IColorProvider {

    private static final String JSON_OBJECT = "object.png";
    private static final String ICON_JSON_ARRAY = "array.png";
    private static final String ICON_JSON_VALUE = "field_protected_obj.png";
    private static final String ICON_JSONNODE = "public_co.png";
    private static final String ICON_ERROR = "error_tsk.png";
    private static final String ICON_INFO = "info_tsk.png";

    private Styler outlineItemTypeStyler = new Styler() {

        @Override
        public void applyStyles(TextStyle textStyle) {
            textStyle.foreground = getColorManager().getColor(HighspeedJSONEditorColorConstants.OUTLINE_ITEM__TYPE);
        }
    };

    @Override
    public Color getBackground(Object element) {
        return null;
    }

    @Override
    public Color getForeground(Object element) {
        return null;
    }

    @Override
    public Image getImage(Object element) {
        if (element == null) {
            return null;
        }
        if (element instanceof Item) {
            Item item = (Item) element;

            ItemType type = item.getItemType();

            if (type == null) {
                return null;
            }

            ItemVariant itemVariant = item.getItemVariant();
            switch (type) {
            case JSON_NODE:
                if (itemVariant == ItemVariant.VALUE) {
                    return getOutlineImage(ICON_JSON_VALUE);
                }
                if (itemVariant == ItemVariant.ARRAY) {
                    return getOutlineImage(ICON_JSON_ARRAY);
                }
                if (itemVariant == ItemVariant.OBJECT) {
                    return getOutlineImage(JSON_OBJECT);
                }
                return getOutlineImage(ICON_JSONNODE);
            case META_ERROR:
                return getOutlineImage(ICON_ERROR);
            case META_INFO:
                return getOutlineImage(ICON_INFO);
            default:
                return null;
            }
        }
        return null;
    }

    @Override
    public StyledString getStyledText(Object element) {
        StyledString styled = new StyledString();
        if (element == null) {
            styled.append("null");
        }
        if (element instanceof Item) {
            Item item = (Item) element;

            String name = item.getName();
            if (name != null) {
                styled.append(name);// +" { ... }");
                styled.append(" ");
            }
            ItemType itemType = item.getItemType();
            if (itemType == ItemType.JSON_NODE) {
                if (item.itemVariant == ItemVariant.VALUE) {
                    StyledString typeString = new StyledString(SimpleStringUtils.shortString(item.getContent(), 20) + " ", outlineItemTypeStyler);
                    styled.append(typeString);
                }
            } else if (itemType == ItemType.META_DEBUG) {
                StyledString typeString = new StyledString(item.getOffset() + ": ", outlineItemTypeStyler);
                styled.append(typeString);
            }

        } else {
            return styled.append(element.toString());
        }

        return styled;
    }

    public ColorManager getColorManager() {
        HighspeedJSONEditorActivator editorActivator = HighspeedJSONEditorActivator.getDefault();
        if (editorActivator == null) {
            return ColorManager.getStandalone();
        }
        return editorActivator.getColorManager();
    }

    private Image getOutlineImage(String name) {
        return EclipseUtil.getImage("/icons/outline/" + name, HighspeedJSONEditorActivator.PLUGIN_ID);
    }

}
