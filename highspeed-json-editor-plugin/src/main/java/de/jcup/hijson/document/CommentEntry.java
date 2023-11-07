package de.jcup.hijson.document;

public class CommentEntry {

    private String comment;

    private int line;
    private int column;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
    
    public void setComment(String comment) {
        this.comment=comment;
    }

    public String getComment() {
        return comment;
    }

}
