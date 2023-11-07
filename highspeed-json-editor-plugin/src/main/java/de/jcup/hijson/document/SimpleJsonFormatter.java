package de.jcup.hijson.document;

import java.util.regex.Pattern;

public class SimpleJsonFormatter {
    
    private static String STRING_PATTERN_SINGLE_LINE_REDUCE="(?m)^[ \t]*\r?\n";
    private static Pattern PATTERN_SINGLE_LINE_REDUCE = Pattern.compile(STRING_PATTERN_SINGLE_LINE_REDUCE);

    public String formatJson(String json) {
        String formatted = json;
        formatted= PATTERN_SINGLE_LINE_REDUCE.matcher(formatted).replaceAll("");
        return formatted;
    }
}
