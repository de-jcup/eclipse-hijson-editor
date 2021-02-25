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
package de.jcup.hijson.document;

import static de.jcup.hijson.document.HighspeedJSONDocumentIdentifiers.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class HighspeedJSONDocumentPartitionScanner extends RuleBasedPartitionScanner {

    private final OnlyLettersKeyWordDetector onlyLettersKeyWordDetector = new OnlyLettersKeyWordDetector(); 
    
	public int getOffset(){
		return fOffset;
	}
	
	public HighspeedJSONDocumentPartitionScanner() {
		IToken comment = createToken(COMMENT);
		IToken doubleString = createToken(STRING);
		IToken nullValue = createToken(NULL);
		IToken key= createToken(KEY);
		IToken bool= createToken(BOOLEAN);

		List<IPredicateRule> rules = new ArrayList<>();
		rules.add(new JSONKeyRule(key));
		rules.add(new SingleLineRule("\"", "\"", doubleString, '\\' , true));
		rules.add(new SingleLineRule("//", "", comment, (char) -1, true));
		rules.add(new MultiLineRule("/*", "*/", comment, (char)-1, true));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector,"false", "", bool));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector,"true", "", bool));
		rules.add(new WordPatternRule(onlyLettersKeyWordDetector,"null", "", nullValue));

		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
	}


	private IToken createToken(HighspeedJSONDocumentIdentifier identifier) {
		return new Token(identifier.getId());
	}
}
