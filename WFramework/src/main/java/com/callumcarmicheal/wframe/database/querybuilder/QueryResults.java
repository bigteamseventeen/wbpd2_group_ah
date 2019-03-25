package com.callumcarmicheal.wframe.database.querybuilder;

public class QueryResults<T> {
    public T[] Rows;
    public boolean Successful = false;
    public int Count = 0;

    public QueryResults() {
        this.Rows = null;
        this.Successful = false;
    }

    public QueryResults(boolean state, T[] rows) {
        this.Successful = state;
        this.Rows = rows;

        if (this.Rows != null)
            this.Count = this.Rows.length;
    }
}