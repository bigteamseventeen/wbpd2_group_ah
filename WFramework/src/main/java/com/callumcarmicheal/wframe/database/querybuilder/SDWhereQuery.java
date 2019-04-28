package com.callumcarmicheal.wframe.database.querybuilder;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.callumcarmicheal.wframe.database.DatabaseModel;
import com.callumcarmicheal.wframe.database.Helper;
import com.callumcarmicheal.wframe.database.Helper.SQLOrderType;

/**
 * Single Dimension Where Query
 */
@SuppressWarnings("rawtypes")
public class SDWhereQuery<T> {

    // Model and queries
    DatabaseModel<T> modelClass;
    LinkedList<QueryType> query;

    // Bound parameters
    private int boundParams = 0;
    public ArrayList<Object> boundParameters = new ArrayList<>();

    // Sql attributes
    private int attr_limit = 0;
    private int attr_offset = 0;
    private SQLOrderType attr_order_type = SQLOrderType.ASC;
    private String attr_order_column = "";

    /**
     * Create a new single dimension query
     * @param modelType     The model instance
     * @param column        The column
     * @param comparison    The comparison
     * @param value         The value to be queried against
     */
    public SDWhereQuery(DatabaseModel<T> modelType, String column, String comparison, Object value) {
        this.modelClass = modelType;

        query = new LinkedList<>();
        query.add(new QueryType(this, new ColumnLink(column, comparison, value)));
    }

    /**
     * Create a new single dimension query
     * @param modelType     The model instance
     * @param column        The column
     * @param comparison    The comparison
     * @param value         The value to be queried against
     * @param qvt           Type of value binding
     */
    public SDWhereQuery(DatabaseModel<T> modelType, String column, String comparison, Object value, QueryValueType qvt) {
        this.modelClass = modelType;

        query = new LinkedList<>();
        query.add(new QueryType(this, new ColumnLink(column, comparison, value), qvt));
    }

    public SDWhereQuery<T> andWhere(String column, String comparison, Object value) {
        query.add(new QueryType(this, "AND", new ColumnLink(column, comparison, value)));
        return this;
    }

    public SDWhereQuery<T> andWhere(String column, String comparison, Object value, QueryValueType qvt) {
        query.add(new QueryType(this, "AND", new ColumnLink(column, comparison, value), qvt));
        return this;
    }

    public SDWhereQuery<T> orWhere(String column, String comparison, Object value) {
        query.add(new QueryType(this, "OR", new ColumnLink(column, comparison, value)));
        return this;
    }

    public SDWhereQuery<T> orWhere(String column, String comparison, Object value, QueryValueType qvt) {
        query.add(new QueryType(this, "OR", new ColumnLink(column, comparison, value), qvt));
        return this;
    }

    public int              getOffset() { return this.attr_offset; }
    public SDWhereQuery<T>  setOffset(int v) { this.attr_offset = v; return this; }
    public int              getLimit() { return this.attr_limit; }
    public SDWhereQuery<T>  setLimit(int v) { this.attr_limit = v; return this; }
    public String           getOrderBy() { return this.attr_order_column; }
    public SDWhereQuery<T>  setOrderBy(String v) { this.attr_order_column = v; return this; }
    public SQLOrderType     getOrderByType() { return this.attr_order_type; }
    public SDWhereQuery<T>  setOrderByType(SQLOrderType v) { this.attr_order_type = v; return this; }

    private String generateSqlQuery() {
        // Query format
        String sql_where = "";

        // Loop the queries
        for (int x = 0; x < query.size(); x++) {
            QueryType qt = query.get(x);

            // If we are not the first one
            if (x != 0)
                sql_where += " " + qt.Type + " ";

            sql_where += qt.toString();
        }

        // Return the sql
        return String.format("SELECT * FROM %s WHERE %s %s", modelClass.orm_getTable(), sql_where, generateSqlAttributes());
    }

    private String generateSqlAttributes() {
        String attributes = "";

        if (this.attr_order_column != "") {
            attributes += "ORDER BY" + attr_order_column;

            switch(attr_order_type) {
                case ASC:  attributes += " ASC  "; break;
                case DESC: attributes += " DESC  "; break;
            }
        }

        if (this.attr_limit > 0)
            attributes += "LIMIT " + this.attr_limit + ", ";
        
        if (this.attr_offset > 0)
            attributes += "OFFSET " + this.attr_offset + ", ";

        if (attributes != "") {
            attributes = attributes.substring(0, attributes.length() - 2);
        }
        
        return attributes;
    }

    /**
     * Execute query
     * @return
     * @throws SQLException
     */
    public QueryResults<T> execute() throws SQLException {
        // Generate the SQL statement
        String sql = generateSqlQuery();

        //logger.debug("SQL:    " + sql);
        //logger.debug("Params: " + this.boundParameters);

        Connection con = modelClass.getConnection();
        Statement stmt = null;
        ResultSet resultSet;

        // If we have any bound parameters create a prepared statement
        if (boundParams > 0) {
            // Create the prepared statement and then bind the parameters
            PreparedStatement pstmt = con.prepareStatement(sql);
            Helper.BindArrayToPreparedStatement(pstmt, boundParameters.toArray());

            stmt = pstmt;
            resultSet = pstmt.executeQuery();
        }
        
        // If we dont have any parameter statements 
        else {
            // Create a normal sql statement
            stmt = con.createStatement();

            // Execute the sql
            resultSet = stmt.executeQuery(sql);
        }

        // Setup the output variables
        QueryResults<T> queryResults = new QueryResults<>();
        ArrayList<T> rowsArray = new ArrayList<>();

        // Loop the rows in the database
        while (resultSet.next()) {
            queryResults.Length++; // Increase the row count
            
            // Create the new instance
            T instance = (T)Helper.NewModelInstance(modelClass);

            // Error handling
            if (instance == null)
                continue;

            DatabaseModel<T> dmInstance = (DatabaseModel<T>)instance;
            dmInstance.orm_parseResultSet(resultSet);
            rowsArray.add(instance);
        }

        // Clear up resources
        resultSet.close();
        stmt.close();

        if (queryResults.Length > 0)
            queryResults.Successful = true;

        if (rowsArray.size() > 0) 
            queryResults.Rows = Helper.ListToGenericArray(rowsArray);
        return queryResults;
    }

    

    public enum QueryValueType {
        /** 
         * Information is attached via a prepared statement
         */
        Bound,
        /**
         * Information is surrounded with parentheses
         * 
         * <p><b>
         * WARNING: If the object does not support toString unintended results may be produced such as
         * className#1234CDE as java automatically generates a hash depending on the input.
         * </b></p>
         * 
         * <p>
         * For example, let NN be the input value
         * <pre><code>column = 'NN'</code></pre>
         * </p>
         */
        String,
        /**
         * The value is treated as a string and is appened inside the SQL statement
         * 
         * <p><b>WARNING: This is unsafe, use bound if the input must be sanitised</b></p>         
         *
         * <p>
         * For example, let NN be the input value (<i>no protection</i>)
         * <pre><code>column = NN</code></pre>
         * </p>
         */
        Raw
    }

    public class QueryType {
        public String Type = "";
        public ColumnLink Query;
        private SDWhereQuery Where;
        public QueryValueType ValueType = QueryValueType.Bound; // States if we are binding information by default its true
        
        String value = ""; // This is the value part of the query COLUMN {EQ} VALUE
        
        public QueryType(SDWhereQuery<T> instance, ColumnLink cl) {
            this.Where = instance;
            this.Query = cl;

            this.generateValue();
        }

        public QueryType(SDWhereQuery<T> instance, ColumnLink cl, QueryValueType valueType) {
            this.Where = instance;
            this.Query = cl;
            this.ValueType = valueType;

            this.generateValue();
        }
        
        public QueryType(SDWhereQuery<T> instance, String columnLink, ColumnLink cl) {
            this.Where = instance;
            this.Type = columnLink;
            this.Query = cl;

            this.generateValue();
        }

        public QueryType(SDWhereQuery<T> instance, String type, ColumnLink cl, QueryValueType valueType) {
            this.Where = instance;            
            this.Type = type;
            this.Query = cl;
            this.ValueType = valueType;

            this.generateValue();
        }

        void generateValue() {
            if (this.ValueType == QueryValueType.Bound) {
                // Removed as JDBC does not support named parameters
                // value = ":sdwq_" + (boundParams++);
                // where.boundParameters.put(value, this.Query.value);

                this.value = "?";
                Where.boundParameters.add(this.Query.value);
                boundParams++;
            } 

            else if (this.ValueType == QueryValueType.String) {
                this.value = String.format("'%s'", this.Query.value);
            }
            
            else if (this.ValueType == QueryValueType.Raw) {
                value = this.Query.value.toString();
            }
        }

        @Override
        public String toString() {
            return String.format("`%s` %s %s", this.Query.column, this.Query.comp, value);
        }
    }
}