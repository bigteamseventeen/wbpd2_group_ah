package com.callumcarmicheal.wframe.database.querybuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.callumcarmicheal.wframe.database.DatabaseModel;

/**
 * Single Dimension Where Query
 */
@SuppressWarnings("rawtypes")
public class SDWhereQuery<T> {
    DatabaseModel<T> modelClass;
    LinkedList<QueryType> query;
    private int boundParams = 0;
    public ArrayList<Object> boundParameters = new ArrayList<>();

    public SDWhereQuery(DatabaseModel<T> t, String column, String comparison, String value) {
        this.modelClass = t;

        query = new LinkedList<>();
        query.add(new QueryType(this, new ColumnLink(column, comparison, value)));
    }

    public SDWhereQuery<T> andWhere(String column, String comparison, String value) {
        query.add(new QueryType(this, "AND", new ColumnLink(column, comparison, value)));
        return this;
    }

    public SDWhereQuery<T> orWhere(String column, String comparison, String value) {
        query.add(new QueryType(this, "OR", new ColumnLink(column, comparison, value)));
        return this;
    }

    private String generateSqlQuery() {
        String sql = String.format("SELECT * FROM %s WHERE ", modelClass.orm_getTable());

        for(int x=0; x < query.size(); x++) {
            QueryType qt = query.get(x);
            
            // If we are not the first one 
            if (x != 0) sql += " " + qt.Type + " ";
            
            sql += qt.toString(); 
        }
        
        return sql;
    }

    private void setupPreparedStatement(PreparedStatement pstmt) {
        Object[] values = boundParameters.toArray();

        for(int x = 0; x < values.length; x++) {
            Object o = values[x];

            // TODO: Bind the parameters here
            if (o instanceof String) {

            }
        }
    }

    public QueryResults<T> Execute() throws SQLException {
        // Generate the SQL statement
        String sql = generateSqlQuery();

        Connection con = modelClass.getConnection();
        Statement stmt = null;

        // Support non parametered statements
        if (boundParams > 0) {
            PreparedStatement pstmt = con.prepareStatement(sql);
            setupPreparedStatement(pstmt);
        } else {
            stmt = con.prepareStatement(sql);
        }
        
        return new QueryResults<>(false, null);
    }

    public enum QueryValueType {
        Bound, 
        String,
        Raw
    }

    public class QueryType {
        public String Type = "";
        public ColumnLink Query;
        private SDWhereQuery where;
        public QueryValueType ValueType = QueryValueType.Bound; // States if we are binding information by default its true
        
        String value = ""; // This is the value part of the query COLUMN {EQ} VALUE
        
        public QueryType(SDWhereQuery sdwq, ColumnLink cl) {
            this.where = sdwq;
            this.Query = cl;

            this.generateValue();
        }
        
        public QueryType(SDWhereQuery sdwq, String type, ColumnLink cl) {
            this.where = sdwq;            
            this.Type = type;
            this.Query = cl;

            this.generateValue();
        }

        public QueryType(SDWhereQuery sdwq, String type, ColumnLink cl, QueryValueType valueType) {
            this.where = sdwq;            
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
                where.boundParameters.add(this.Query.value);
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
            return String.format("%s %s %s", this.Query.column, this.Query.comp, value);
        }
    }
}