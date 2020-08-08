package de.jcup.jsoneditor.document;

import org.eclipse.jface.text.IDocument;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONFormatSupport {

    public class FormatterResult{
        public FormatterResult(FormatterResultState state) {
            this.state=state;
        }
        public FormatterResult(FormatterResultState state, String message) {
            this(state);
            this.message=message;
        }
        public FormatterResultState state;
        public String message;
        public int line;
        public int column;
        public long offset;
        
    }
    
    public enum FormatterResultState {

        KEPT_AS_IS,

        FORMAT_DONE,

        NOT_VALID_JSON_BUT_FALLBACK_DONE,

        NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS,

    }

    public static final JSONFormatSupport DEFAULT = new JSONFormatSupport();
    private static final ObjectMapper mapper = new ObjectMapper();

    public FormatterResult formatJSONIfNotHavingMinAmountOfNewLines(IDocument document) {
        FormatterResult result = formatJSONIfNotHavingMinAmountOfNewLines(document, 3,1000);
        
        return result;
    }
    
    public JsonNode read(String json) throws JsonMappingException, JsonProcessingException {
        return mapper.readTree(json);
    }
    
    FormatterResult formatJSONIfNotHavingMinAmountOfNewLines(IDocument document, int minNewLines, int minLengthWithoutAutoChange) {
        if (document == null) {
            return new FormatterResult(FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS);
        }
        String str = document.get();
        if (str == null) {
            return new FormatterResult(FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS,"empty string is no valid json");
        }
        if (str.length() < minLengthWithoutAutoChange) {
            return new FormatterResult(FormatterResultState.KEPT_AS_IS);
        }
        int count = 0;
        int pos = 0;
        while (count < minNewLines && pos < 2000) {
            char c = str.charAt(pos);
            if (c == '\n') {
                count++;
            }
            pos++;
        }
        if (count < minNewLines) {
            return formatJSONAndUpdateDocument(document);
        }
        return new FormatterResult(FormatterResultState.KEPT_AS_IS);
    }

    public FormatterResult formatJSON(String text) {
        return formatJSON(null, text);
    }
    
    public FormatterResult formatJSONAndUpdateDocument(IDocument document) {
        if (document == null) {
            return new FormatterResult(FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS,"document null not acceptable");
        }
        String text = document.get();
        return formatJSON(document,text);
    }
    
    private FormatterResult formatJSON(IDocument document, String text) {
        if (text == null) {
            return new FormatterResult(FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS,"text null not acceptable");
        }
        String formatted;
        try {
            formatted = formatIt(text);
            if (document!=null) {
                document.set(formatted);
            }
            return new FormatterResult(FormatterResultState.FORMAT_DONE);
        } catch (JsonProcessingException e) {
            
            JsonProcessingException inspect=null;
            
            FormatterResult result = formatJSONByFallback(document,text);
            if (result.state==FormatterResultState.NOT_VALID_JSON_BUT_FALLBACK_DONE) {
                try {
                    formatIt(document.get());
                }catch(JsonProcessingException e2) {
                    inspect=e2;
                }
            }else {
                inspect = e;
            }
            if (inspect==null) {
                result.message="illegal state - after fallback format for invalid json - json has become valid?!?!?!";
            }else {
                JsonLocation location = inspect.getLocation();
                result.column = location.getColumnNr();
                result.line = location.getLineNr();
                result.offset = location.getCharOffset();
                result.message=inspect.getMessage();
            }
            return result;
        }

    }

    private String formatIt(String text) throws JsonProcessingException, JsonMappingException {
        String indented;
        Object json = mapper.readValue(text, Object.class);
        indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        return indented;
    }

    private FormatterResult formatJSONByFallback(IDocument document, String text) {
        if (text.indexOf("{\n") == -1) {
            String result = text.replaceAll("\\{", "\\{\n");
            document.set(result);
            return new FormatterResult(FormatterResultState.NOT_VALID_JSON_BUT_FALLBACK_DONE);
        }

        return new FormatterResult(FormatterResultState.NOT_VALID_JSON_NO_FALLBACK_POSSIBLE_SO_KEEP_AS_IS);
    }

}
