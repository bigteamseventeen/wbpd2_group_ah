package com.callumcarmicheal.wframe.database.querybuilder;

public class QueryResults<T> {
    /** Rows returned from the database */
    public T[] Rows;
    /** If the query has returned any results */
    public boolean Successful = false;
    /** The length of the row's returned (array size) */
    public int Length = 0;

    /**
     * Create a QueryResults store with defaults
     * 
     * <p> To store the query results you could proceed with something like this:
     * <pre><code>QueryResults queryResults = new QueryResults();
     * queryResults.Rows = databaseResultsParsed.toArray();
     * queryResults.Length = queryResults.Rows.Length;
     * queryResults.Success = queryResults.Length > 0;</code>
     * </pre></p>
     */
    public QueryResults() {
        this.Rows = null;
        this.Successful = false;
    }

    /**
     * Create a QueryResults store with existing values
     * 
     * @param rows The data set
     */
    public QueryResults(T[] rows) {
        this.Rows = rows;

        // Set the values if we have rows
        if (this.Rows != null) {
            this.Length = this.Rows.length;
            this.Successful = this.Rows.length > 0;
        }
    }

    /**
     * Convert 
     */
    public String toString() {
        // TODO: Implement ToString QueryResults
        return "";

        // if (Rows == null)
        //     return "QueryResults {"

        // return "QueryResults { Successful=" + Successful + ", Length=" + Length + ", Rows=" + Rows.toString() + "}";
    }
}