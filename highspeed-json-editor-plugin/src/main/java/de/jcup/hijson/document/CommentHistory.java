package de.jcup.hijson.document;

import java.util.ArrayList;
import java.util.List;

public class CommentHistory {

    
   List<CommentEntry> list = new ArrayList<>();
    
    public void addComment(int line, int column, String comment) {
        CommentEntry entry = new CommentEntry();
        entry.setComment(comment);
        entry.setLine(line);
        entry.setColumn(column);
    }

}
