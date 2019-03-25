package com.callumcarmicheal.wframe.database;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.callumcarmicheal.wframe.HttpRequest;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery;

@SuppressWarnings("rawtypes")
public abstract class DatabaseModel<T> {
    protected Connection connection;
    protected String table = "";
    protected boolean initialized = false;
    protected DatabaseColumn primaryKey = null;
    protected LinkedHashMap<String, DatabaseColumn> columns = new LinkedHashMap<>();
    protected HashMap<String, DatabaseColumnValue> values = new LinkedHashMap<>();

    protected DatabaseModel() {
        for(String x : columns.keySet())
            values.put(x, new DatabaseColumnValue(columns.get(x)));
    }

    public Connection getConnection() {
        return this.connection;
    }

    protected void AddColumn(DatabaseColumn db) {
        columns.put(db.name, db);
    }

    protected boolean CreateTable(boolean IfNotExists) {
        String sqlColumns  = "\n";

        for (String x : columns.keySet()) 
            sqlColumns += "    " + columns.get(x).getColumnDefition() + "\n";
        sqlColumns = sqlColumns.substring(0, sqlColumns.length() - 2) + "\n";

        String sql = String.format("CREATE TABLE %s %s(%s);", IfNotExists ? "IF NOT EXISTS ":"", table + " ", sqlColumns);
        
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception ex) {
            System.err.println("Failed to create table for " + table);
            System.err.println("  Sql: \n" + sql);
            ex.printStackTrace();

            System.exit(1);
            return false;
        }
        
        return true;
    }

    public static <T> SDWhereQuery where(DatabaseModel<T> model, String column, String comparison, String value) {
        return new SDWhereQuery<T>(model, column, comparison, value);
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

    public void save() throws SQLException {

    }
}