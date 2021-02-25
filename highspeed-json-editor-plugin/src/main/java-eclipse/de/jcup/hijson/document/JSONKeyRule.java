package de.jcup.hijson.document;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * When we have <code>
 * {
 *  "key1" : "value1"
 *  }
 *  </code> This rule shall accept "key1" but not "value1". So
 * 
 * @author albert
 *
 */
public class JSONKeyRule implements IPredicateRule {

    private IToken token;
    boolean trace;

    public JSONKeyRule(IToken token) {
        this.token = token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    @Override
    public IToken getSuccessToken() {
        return token;
    }

    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        ICharacterScannerCodePosSupport support = new ICharacterScannerCodePosSupport(scanner);
        int pos = support.getInitialStartPos();

        // we scan for two ", followed by whitespace (optional) and then a colon :
        Character c = null;
        int countExlamationMarks = 0;
        boolean foundColon = false;
        boolean cancelSearch = false;
        while (!cancelSearch) {
            c = support.getCharacterAtPosOrNull(pos++);
            if (c == null) {
                break;
            }
            char cv = c.charValue();
            if (countExlamationMarks == 0) {
                if (cv != '"') {
                    // first char is not " so not a key - fast fail
                    break;
                }
                countExlamationMarks++;
            } else if (countExlamationMarks == 1) {
                if (cv == '"') {
                    countExlamationMarks++;
                }
            } else if (countExlamationMarks == 2) {
                if (Character.isWhitespace(cv)) {
                    /* whitespaces are accepted */
                    continue;
                }
                cancelSearch = true;
                if (cv == ':') {
                    foundColon = true;
                }
            } else {
                cancelSearch = true;
            }
        }
        if (foundColon) {
            return token;
        }else {
            support.resetToStartPos();
            return Token.UNDEFINED;
        }
    }

}