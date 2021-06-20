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

import org.eclipse.jface.viewers.ITreeContentProvider;

import de.jcup.hijson.script.HighspeedJSONModel;

public class HighspeedJSONEditorTreeContentProvider2 implements ITreeContentProvider {

    private static final String JSON_MODEL_EMPTY_OR_INVALID = "Empty JSON or invalid";
    private static final String JSON_MODEL_DISABLED = "Outline disabled - must be enabled by menu or toolbar";
    private static final Object[] RESULT_WHEN_EMPTY = new Object[] { JSON_MODEL_EMPTY_OR_INVALID };

    private Object[] items;
    private Object monitor = new Object();
    boolean outlineEnabled;
    private HighspeedJSONModel model;

    HighspeedJSONEditorTreeContentProvider2() {
        items = RESULT_WHEN_EMPTY;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        synchronized (monitor) {
            if (inputElement != null && !(inputElement instanceof HighspeedJSONModel)) {
                return new Object[] { "Unsupported input element:" + inputElement };
            }
            if (items != null && items.length > 0) {
                return items;
            }
        }
        return RESULT_WHEN_EMPTY;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof Item)) {
            return null;
        }
        Item item = (Item) parentElement;
        return item.children.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (!(element instanceof Item)) {
            return null;
        }
        Item item = (Item) element;
        return item.parent;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (!(element instanceof Item)) {
            return false;
        }
        Item item = (Item) element;
        return !item.children.isEmpty();

    }

    public void rebuildTree(HighspeedJSONModel model) {
        synchronized (monitor) {
            this.model = model;
            Item rootItem = model != null ? model.getRootItem() : null;
            Item item = null;

            if (outlineEnabled && rootItem != null) {
                item = rootItem;
            } else {
                item = new Item();
                if (!outlineEnabled) {
                    item.name = JSON_MODEL_DISABLED;
                } else {
                    item.name = JSON_MODEL_EMPTY_OR_INVALID;
                }
            }
            items = new Item[] { item };
        }
    }

    public Item tryToFindByOffset(int offset) {
        synchronized (monitor) {
            if (model == null) {
                return null;
            }
            if (model.hasErrors()) {
                return null;
            }
            Item item = null;
            for (int i = 0; i < 50 && item == null; i++) {
                int offset2 = offset + i;
                item = model.getItemOffsetMap().get(offset2);

            }
            return item;

        }
    }

}
