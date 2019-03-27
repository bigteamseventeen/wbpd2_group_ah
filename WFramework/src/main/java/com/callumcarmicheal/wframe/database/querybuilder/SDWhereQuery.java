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

/**
 * Single Dimension Where Query
 */
@SuppressWarnings("rawtypes")
public class SDWhereQuery<T> {
    DatabaseModel<T> modelClass;
    LinkedList<QueryType> query;
    private int boundParams = 0;
    public ArrayList<Object> boundParameters = new ArrayList<>();

    public SDWhereQuery(DatabaseModel<T> t, String column, String comparison, Object value) {
        this.modelClass = t;

        query = new LinkedList<>();
        query.add(new QueryType(this, new ColumnLink(column, comparison, value)));
    }

    public SDWhereQuery(DatabaseModel<T> t, String column, String comparison, Object value, QueryValueType qvt) {
        this.modelClass = t;

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

    private String generateSqlQuery() {
        String sql = String.format("SELECT * FROM %s WHERE ", modelClass.orm_getTable());

        for (int x = 0; x < query.size(); x++) {
            QueryType qt = query.get(x);

            // If we are not the first one
            if (x != 0)
                sql += " " + qt.Type + " ";

            sql += qt.toString();
        }

        return sql;
    }

    private void setupPreparedStatement(PreparedStatement pstmt) throws SQLException {
        Object[] values = boundParameters.toArray();

        for (int x = 1; x <= values.length; x++) {
            Object o = values[x - 1];

            if (o instanceof String) {
                pstmt.setString(x, (String) o);
            } else if (o instanceof Integer) {
                pstmt.setInt(x, (Integer) o);
            } else if (o instanceof Boolean) {
                pstmt.setBoolean(x, (Boolean) o);
            } else if (o instanceof Float) {
                pstmt.setFloat(x, (Float) o);
            }

            // Todo: Add more
            else {
                // Just set it as a object
                pstmt.setObject(x, o);
            }
        }
    }

    // Constructor arguemnts
    private static Class[] cArg = null;

    /**
     * Create a new model instance assuming the class has the constructor that passes in a connection
     * 
     * We are using reflection because we cannot create a instance of a generic class
     */
    private DatabaseModel<T> newInstance() {
        // If we have already not created the class constructor definition 
        if (cArg == null) {
            cArg = new Class[1];
            cArg[0] = Connection.class;  
        }
        
        try {
            // Attempt to create a new instance of the modelClass 
            return modelClass.getClass().getDeclaredConstructor(cArg).newInstance(modelClass.getConnection());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public QueryResults<T> Execute() throws SQLException {
        // Generate the SQL statement
        String sql = generateSqlQuery();

        // System.out.println("SQL:    " + sql);
        // System.out.println("Params: " + this.boundParameters);

        Connection con = modelClass.getConnection();
        Statement stmt = null;
        ResultSet resultSet;

        // If we have any bound parameters create a prepared statement
        if (boundParams > 0) {
            // Create the prepared statement and then bind the parameters
            PreparedStatement pstmt = con.prepareStatement(sql);
            setupPreparedStatement(pstmt);

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
            T instance = (T)newInstance();

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

        queryResults.Rows = toArray(rowsArray);
        return queryResults;
    }

    private T[] toArray(List<T> list) {
        Class clazz = list.get(0).getClass(); // check for size and null before
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
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