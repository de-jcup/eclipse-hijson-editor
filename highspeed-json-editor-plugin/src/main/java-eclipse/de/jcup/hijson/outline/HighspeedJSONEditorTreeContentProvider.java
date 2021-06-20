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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import de.jcup.hijson.script.HighspeedJSONError;
import de.jcup.hijson.script.HighspeedJSONModel;

@Deprecated // use HighspeedJSONEditorTreeContentProvider2
public class HighspeedJSONEditorTreeContentProvider implements ITreeContentProvider {

    private static final String JSON_MODEL_CONTAINS_ERRORS = "JSON contains errors.";
    private static final String JSON_MODEL_EMPTY_OR_INVALID = "Empty JSON or invalid";
    private static final String JSON_MODEL_DISABLED = "Outline disabled - must be enabled by menu or toolbar";
    private static final Object[] RESULT_WHEN_EMPTY = new Object[] { JSON_MODEL_EMPTY_OR_INVALID };

    private Object[] items;
    private Object monitor = new Object();
    boolean outlineEnabled;

    HighspeedJSONEditorTreeContentProvider() {
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

    private Item[] createItems(HighspeedJSONModel model) {
        List<Item> list = new ArrayList<>();
        JsonNode rootNode = null;
        if (model != null) {
            rootNode = model.getRootNode();
        }
        if (rootNode == null || !outlineEnabled) {
            Item item = new Item();
            if (!outlineEnabled) {
                item.name = JSON_MODEL_DISABLED;
            } else {
                item.name = JSON_MODEL_EMPTY_OR_INVALID;
            }
            item.type = ItemType.META_INFO;
            item.offset = 0;
            item.length = 0;
            item.endOffset = 0;
            item.variant = "Info";
            list.add(item);
        } else {
            return new Item[] { createNodeItemAndChildren(rootNode, "root") };
        }

        if (model != null && model.hasErrors()) {
            for (HighspeedJSONError error : model.getErrors()) {
                Item item = new Item();
                item.name = JSON_MODEL_CONTAINS_ERRORS;
                item.type = ItemType.META_ERROR;
                item.offset = (int) error.offset;
                item.length = 1;
                item.endOffset = item.offset + 1;
                item.variant = "Error";
                list.add(0, item);
            }
        }
        return list.toArray(new Item[list.size()]);

    }

    private Item createNodeItemAndChildren(JsonNode node, String name) {
        Item item = new Item();
        item.name = name;
        item.type = ItemType.JSON_NODE;
        item.offset = 0; /*
                          * FIXME albert: fix offset calculation - maybe choose jackson stream API
                          * instead tree parse!
                          */
        item.length = 1;
        item.endOffset = item.offset + 1;

        if (node instanceof ArrayNode) {
            ArrayNode an = (ArrayNode) node;
            item.variant = "Array";
            item.name = item.name + "(" + an.size() + ")";
        } else if (node instanceof ObjectNode) {
            item.variant = "Object";
        } else if (node instanceof ValueNode) {
            item.variant = "Value";
        } else {
            item.variant = node.getClass().getSimpleName();
        }

        if (node instanceof ObjectNode) {
            ObjectNode cn = (ObjectNode) node;
            item.children = new ArrayList<Item>();
            for (Iterator<String> it = cn.fieldNames(); it.hasNext();) {
                String childName = it.next();
                JsonNode next = cn.get(childName);
                Item child = createNodeItemAndChildren(next, childName);
                item.children.add(child);
                child.parent = item;
            }
        } else if (node instanceof ArrayNode) {
            ArrayNode cn = (ArrayNode) node;
            int index = 0;
            item.children = new ArrayList<Item>();
            int length = cn.size();
            int pos = 0;
            Item parent = item;
            for (Iterator<JsonNode> it = cn.elements(); it.hasNext();) {
                String childName = name + "[" + index + "]";
                JsonNode next = it.next();
                Item child = createNodeItemAndChildren(next, childName);

                if (length > 100) {
                    if (pos == 0) {
                        parent = new Item();
                        parent.children = new ArrayList<>();
                        parent.variant = "segment";
                        parent.name = "[" + index + "..";
                        int remaining = length - index;
                        if (remaining < 100) {
                            parent.name += index + remaining;
                        } else {
                            parent.name += index + 100;
                        }
                        parent.name += "]";
                        parent.type = ItemType.VIRTUAL_ARRAY_SEGMENT_NODE;

                        item.children.add(parent);
                        parent.parent = item;
                    }
                    pos++;
                    if (pos > 100) {
                        pos = 0;
                    }
                }
                parent.children.add(child);
                child.parent = parent;

                index++;
            }
        }
        return item;
    }

    public void rebuildTree(HighspeedJSONModel model) {
        synchronized (monitor) {
            items = createItems(model);
        }
    }

    public Item tryToFindByOffset(int offset) {
        synchronized (monitor) {
            if (items == null) {
                return null;
            }
            for (Object oitem : items) {
                if (!(oitem instanceof Item)) {
                    continue;
                }
                Item item = (Item) oitem;
                int itemStart = item.getOffset();
                int itemEnd = item.getEndOffset();// old: itemStart+item.getLength();
                if (offset >= itemStart && offset <= itemEnd) {
                    return item;
                }
            }

        }
        return null;
    }

}
