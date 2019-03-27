package com.callumcarmicheal.wframe.database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.database.exceptions.MissingColumnValueException;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery.QueryValueType;
import com.callumcarmicheal.wframe.library.Tuple;

@SuppressWarnings("rawtypes")
public abstract class DatabaseModel<T> {
    private boolean exists = false;

    protected Connection connection;
    protected String table = "";
    protected DatabaseColumn primaryKey = null;
    protected LinkedHashMap<String, DatabaseColumn> columns = new LinkedHashMap<>();
    protected HashMap<String, DatabaseColumnValue> values = new LinkedHashMap<>();

    /**
     * Initializes a instance of the database model
     * @param Table Database Table
     * @param Columns Columns used in the model
     * @param PrimaryKey The models primary key (required)
     * @param DatabaseConnection The database connection
     */
    protected DatabaseModel(String Table, LinkedHashMap<String, DatabaseColumn> Columns, 
            DatabaseColumn PrimaryKey, Connection DatabaseConnection) { 
        // Setup the model instance settings
        table   = "users";
        columns = Columns;
        primaryKey = PrimaryKey;
        connection = DatabaseConnection;

        // Setup the value settings
        for(String x : columns.keySet())
            values.put(x, new DatabaseColumnValue(this, columns.get(x)));
    }

    public Connection getConnection() {
        return this.connection;
    }

    protected void AddColumn(DatabaseColumn db) {
        columns.put(db.name, db);
    }

    /**
     * Generate table schema and execute the sql
     * @param IfNotExists If the "if not exists" flag is used in the sql
     * @return Query Success
     */
    protected boolean CreateTable(boolean IfNotExists) {
        return CreateTable(IfNotExists, true);
    }

    /**
     * Generate table schema and execute the sql
     * @param IfNotExists If the "if not exists" flag is used in the sql
     * @param ExitOnFailure Exits the applicaiton on failure
     * @return Query Success
     */
    protected boolean CreateTable(boolean IfNotExists, boolean ExitOnFailure) {
        // Store the sql columns by getting their definitions
        String sqlColumns  = "\n";
        for (String x : columns.keySet()) 
            sqlColumns += "    " + columns.get(x).getColumnDefition() + "\n";

        // Remove any trailing ",\n"
        sqlColumns = sqlColumns.substring(0, sqlColumns.length() - 2) + "\n";

        // Generate the table sql
        String sql = String.format("CREATE TABLE %s %s(%s);", IfNotExists ? "IF NOT EXISTS ":"", 
            table + " ", sqlColumns);
        
        try {
            // Attempt to execute the sql
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();

            // Query successfully executed
            return true;
        } catch (SQLException ex) {
            // Print the basic sql information
            System.err.println("Failed to create table for " + table);
            System.err.println("  Sql: \n" + sql);

            // Print the exception information
            ex.printStackTrace();

            // If we want to exit on failure
            if (ExitOnFailure) System.exit(1);
            return false; // We have failed to execute the query
        }
    }

    /**
     * Query the database
     * @param <T> Database Model
     * @param model A instance of the model
     * @param column The column
     * @param comparison The seperation, EG: < > <= >= = "LIKE"
     * @param value The value being searched for
     * @return A instance of the query generator
     */
    public static <T> SDWhereQuery where(DatabaseModel<T> model, String column, String comparison, Object value) {
        return new SDWhereQuery<T>(model, column, comparison, value);
    }

    /**
     * Query the database
     * @param <T> Database Model
     * @param model A instance of the model
     * @param column The column
     * @param comparison The seperation, EG: < > <= >= = "LIKE"
     * @param value The value being searched for
     * @param qvt How the SQL is generated for the value
     * @return A instance of the query generator
     */
    public static <T> SDWhereQuery where(DatabaseModel<T> model, String column, String comparison, Object value, QueryValueType qvt) {
        return new SDWhereQuery<T>(model, column, comparison, value, qvt);
    }

    /**
     * Parses resultset from a Query
     * @param rs
     * @throws SQLException
     */
    public void orm_parseResultSet(ResultSet rs) throws SQLException {
        orm_parseResultSet(rs, true);
    }

    public void orm_parseResultSet(ResultSet rs, boolean markAsExisting) throws SQLException {
        if (markAsExisting)
            exists = true;

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        
        // The column count starts from 1
        for (int i = 1; i <= columnCount; i++ ) {
            String col = rsmd.getColumnName(i);
            
            if (values.keySet().contains(col)) {
                DatabaseColumnValue store = values.get(col);
                store.Value = rs.getObject(col);
                // System.out.println(col + ": " + store.Value);
            }
        }

        // The code above will only append columns in the query oposed to the code below which
        // would've caused issues if there was a column missing in the results
        //
        //// for (Map.Entry<String, DatabaseColumnValue> entry : values.entrySet()) {
        ////     String column = entry.getKey();
        ////     DatabaseColumnValue store = entry.getValue();
        ////     store.Value = rs.getObject(column);
        ////     System.out.println(column + ": " + store.Value);
        //// }
    }

    public DatabaseColumn orm_getPrimaryKey() {
        return primaryKey;
    }

    public LinkedHashMap<String, DatabaseColumn> orm_getColumns() {
        return columns;
    }

    public String orm_getTable() {
        return table;
    }

    public boolean isNew() {
        return exists;
    }

    // TODO: Add a parameter to save that allows the verification of column nullables to be disabled when updating

    /**
     * Save changes to database without dealing with exceptions
     * @return If the query was successful
     */
    public boolean save_s() {
        try {
            return save(true);
        } catch (Exception ex) {
            return false;
        }
    }

    /** 
     * Save changes to database without dealing with exceptions
     * @param updateOnly 
     * @return If the query was successful
     */
    public boolean save_s(boolean updateOnly) {
        try {
            return save(updateOnly);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Save changes to the database
     * @return If the query was successful
     * @throws SQLException The exception during the query
     * @throws MissingColumnValueException The constraint validation
     */
    public boolean save() throws SQLException, MissingColumnValueException {
        return save(true);
    }

    /**
     * Save changes to the database
     * @param updateOnly States if only the modified columns are updated (ONLY APPLIES TO EXISTING ROWS)
     * @return If the query was successful
     * @throws SQLException The exception during the query
     * @throws MissingColumnValueException The constraint validation
     */
    public boolean save(boolean updateOnly) throws SQLException, MissingColumnValueException {
        // SQL Queries
        String sql = "";

        // Prepared Statement parameters
        ArrayList<Object> stmt_bind = new ArrayList<Object>();

        Tuple<Boolean, String> sqlGenerate = this.exists 
            ? generateSave_Insert(updateOnly, stmt_bind) 
            : generateSave_Update(updateOnly, stmt_bind);

        // We failed to generate a query
        if (sqlGenerate.x == false) 
            return false;

        // Get the query
        sql = sqlGenerate.y;
        
        // Query Statement
        Statement stmt;
        int update;

        // If we have any bound parameters create a prepared statement
        if (stmt_bind.size() > 0) {
            // Create the prepared statement and then bind the parameters
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            // Execute our query
            update = pstmt.executeUpdate();

            // Set our statement
            stmt = pstmt;
        }

        // If we dont have any parameter statements 
        else {
            // Create a normal sql statement
            stmt = connection.createStatement();
            update = stmt.executeUpdate(sql);
        }

        // Clear up resources
        stmt.close();

        return true;
    }

    private Tuple<Boolean, String> generateSave_Insert(boolean updateOnly, ArrayList<Object> stmt_bind)
            throws MissingColumnValueException {
        String sql = "";
        String sql_insert = "INSERT INTO %s (%s) VALUES (%s);"; // Sql statement format
        String sql_col = ""; // Remove trailing ", "
        String sql_val = ""; // Remove trailing ", "

        for (Map.Entry<String, DatabaseColumnValue> entry : values.entrySet()) {
            String col = entry.getKey();
            DatabaseColumnValue store = entry.getValue();

            // Check if we have any constraints (non-nullable columns) that are required
            if (!store.isValid())
                throw new MissingColumnValueException(store);

            // If we are only updating modified columns (shortens sql queries)
            if (updateOnly && !store.isModified())
                continue;
            
            // Add to the column list
            sql_col += (store.Column.name + ", ");
            
            // Check if the data is null
            if (store.isNullable && store.Value == null) {
                // We are just adding NULL to the query
                sql_val += "NULL, ";
            }

            // We have a value
            else {
                // Switch the method of handing the data
                switch(store.Column.dataHandlingMethod) {
                    case Bound: // We are binding the information
                        sql_val += "?, ";
                        stmt_bind.add(store.Value);
                        break;
                    case String: // We are calling toString and then just adding it to the query with quotes
                        sql_val += String.format("'%s', ", store.Value);
                        break;
                    case Raw: // We are calling toString without any surrounding quotes
                        sql_val += store.Value.toString() + ", ";
                        break;
                }
            }
        }

        // We have nothing to commit to the database
        if (sql_col == "" || sql_val == "")
            return new Tuple<>(false, "");

        // Remove any trailing ", "
        sql_col = sql_col.substring(0, sql_col.length() - 2);
        sql_val = sql_val.substring(0, sql_val.length() - 2);
        sql = String.format(sql_insert, table, sql_col, sql_val);
        
        return new Tuple<>(true, sql);
    }

    private Tuple<Boolean, String> generateSave_Update(boolean updateOnly, ArrayList<Object> stmt_bind)
            throws MissingColumnValueException {
        String sql_update = "UPDATE %s SET %s WHERE %s=%s;"; // Sql statement format
                
        // TODO: Create the code to generate a update sql statement
        return new Tuple<>(false, "");
    }
}