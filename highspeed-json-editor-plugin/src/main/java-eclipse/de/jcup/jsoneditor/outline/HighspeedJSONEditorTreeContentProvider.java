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
package de.jcup.jsoneditor.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

import com.fasterxml.jackson.databind.JsonNode;

import de.jcup.jsoneditor.script.HighspeedJSONError;
import de.jcup.jsoneditor.script.HighspeedJSONScriptModel;

public class HighspeedJSONEditorTreeContentProvider implements ITreeContentProvider {

	private static final String BATCH_SCRIPT_CONTAINS_ERRORS = "HighspeedJSON script contains errors.";
	private static final String BATCH_SCRIPT_DOES_NOT_CONTAIN_ANY_LABELS = "HighspeedJSON script does not contain any labels";
	private static final Object[] RESULT_WHEN_EMPTY = new Object[] { BATCH_SCRIPT_DOES_NOT_CONTAIN_ANY_LABELS };
	private Object[] items;
	private Object monitor = new Object();

	HighspeedJSONEditorTreeContentProvider() {
		items = RESULT_WHEN_EMPTY;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		synchronized (monitor) {
			if (inputElement!=null && !(inputElement instanceof HighspeedJSONScriptModel)) {
				return new Object[] { "Unsupported input element:"+inputElement };
			}
			if (items != null && items.length > 0) {
				return items;
			}
		}
		return RESULT_WHEN_EMPTY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

	private Item[] createItems(HighspeedJSONScriptModel model) {
		List<Item> list = new ArrayList<>();
		JsonNode rootNode = model.getRootNode();
		if (rootNode==null) {
		    Item item = new Item();
            item.name = BATCH_SCRIPT_DOES_NOT_CONTAIN_ANY_LABELS;
            item.type = ItemType.META_INFO;
            item.offset = 0;
            item.length = 0;
            item.endOffset=0;
            list.add(item);
		}else {
		    return new Item[] {createitem(rootNode)};
		}
		if (model.hasErrors()) {
		    for (HighspeedJSONError error: model.getErrors()) {
		        Item item = new Item();
		        item.name = BATCH_SCRIPT_CONTAINS_ERRORS;
		        item.type = ItemType.META_ERROR;
		        item.offset = (int) error.offset;
		        item.length = 1;
		        item.endOffset=item.offset+1;
		        list.add(0, item);
		    }
		}
		return list.toArray(new Item[list.size()]);

	}

	private Item createitem(JsonNode node) {
	    Item item = new Item();
        item.name = node.asText("[unknown]");
        item.type = ItemType.LABEL;
        item.offset = 0; /* FIXME albert: fix offset calculation!*/ 
        item.length = 1;
        item.endOffset=item.offset+1;
        return item;
    }

    public void rebuildTree(HighspeedJSONScriptModel model) {
		synchronized (monitor) {
			if (model == null) {
				items = null;
				return;
			}
			items = createItems(model);
		}
	}

	public Item tryToFindByOffset(int offset) {
		synchronized (monitor) {
			if (items==null){
				return null;
			}
			for (Object oitem: items){
				if (!(oitem instanceof Item)){
					continue;
				}
				Item item = (Item) oitem;
				int itemStart = item.getOffset();
				int itemEnd = item.getEndOffset();// old: itemStart+item.getLength();
				if (offset >= itemStart && offset<=itemEnd ){
					return item;
				}
			}
			
		}
		return null;
	}

}
