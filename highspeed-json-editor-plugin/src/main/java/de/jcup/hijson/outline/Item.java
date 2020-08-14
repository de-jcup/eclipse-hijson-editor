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

import java.util.Collections;
import java.util.List;

public class Item {

	ItemType type;
	String name;
	String variant;
	int offset;
	int length;
	int endOffset;
	Item parent;
    List<Item> children =Collections.emptyList(); // initial only empty list
	
	/**
	 * @return item type , or <code>null</code>
	 */
	public ItemType getItemType(){
		return type;
	}

	public String getName() {
		return name;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}
	
	public int getEndOffset() {
		return endOffset;
	}
	
	public String getVariant() {
        return variant;
    }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Item:");
		sb.append("name:");
		sb.append(name);
		sb.append(",type:");
		sb.append(type);
		sb.append(",variant:");
		sb.append(variant);
		sb.append(",offset:");
		sb.append(offset);
		sb.append(",length:");
		sb.append(length);
		sb.append(",endOffset:");
		sb.append(endOffset);
		return sb.toString();
	}

	public String buildSearchString() {
		return name;
	}
}
