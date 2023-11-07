package de.jcup.hijson.document;

public class CommentHistorySupport {
    
    private SimpleJsonFormatter simpleFormatter = new SimpleJsonFormatter();
    
    public CommentHistory ceateCommentHistory(String json) {
        CommentHistory history = new CommentHistory();
        /* FIXME de-jcup , 2023: handle comments hwich are in own line special: must be removed for line handling
         * means simple formatter must have two ways of formatting... first the new line comments are contained, 
         * afterwards remove those as well to get the target line numbers...  */
        String jsonWithoutEmptyNewLines= simpleFormatter.formatJson(json);
        String[] lines = jsonWithoutEmptyNewLines.split("\n");
        
        for (int lineNumber=0;lineNumber<lines.length;lineNumber++) {
            String line = lines[lineNumber].trim();
            int column = line.indexOf("//");
            if (column!=-1) {
                history.addComment(lineNumber, column, line.substring(column));
            }
        }
        
        return history;
    }
    
    public String applyCommentsToFormattedJson(String json, CommentHistory history) {
        StringBuilder sb=new StringBuilder();
        String[] lines = json.split("\n");
        for (int lineNumber=0;lineNumber<lines.length;lineNumber++) {
            String line = lines[lineNumber];
            /* FIXME de-jcup , 2023: implement add of comments which are NOT at the beginning */
            /* FIXME de-jcup , 2023: implement add of comments which ARE at the beginning - means we got a special numbering mechanism here*/
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
