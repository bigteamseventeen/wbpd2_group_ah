package com.callumcarmicheal.wframe.database.querybuilder;

public class ColumnLink {
    public String column;
    public String comp;
    public Object value;

    public ColumnLink(String col, String comp, Object val) {
        this.column = col;
        this.comp = comp;
        this.value = val;
    }
}