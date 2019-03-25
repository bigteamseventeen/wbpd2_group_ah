package com.bigteamseventeen.g1.milestones.models;

import com.callumcarmicheal.wframe.database.CInteger;
import com.callumcarmicheal.wframe.database.CVarchar;
import com.callumcarmicheal.wframe.database.DatabaseColumn;
import com.callumcarmicheal.wframe.database.DatabaseModel;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedHashMap;

@SuppressWarnings("rawtypes")
public class User extends DatabaseModel<User> {
    // -----------------------------------------------------------
    // --                                                       --
    // ------------- Model Definition Attributes ----------------- 
    // --                                                       --
    // -----------------------------------------------------------
    // Creating these for each instance would be taxing so we want to cache them
    private static LinkedHashMap<String, DatabaseColumn> _ColumnsDefinition = new LinkedHashMap<>();
    private static CInteger _PrimaryKey;

    public User(Connection c) { 
        // Setup the model instance settings
        table   = "users";
        columns = _ColumnsDefinition;
        primaryKey = _PrimaryKey;
        connection = c;
    }

    private static void _addColumn(DatabaseColumn db) {
        _ColumnsDefinition.put(db.GetName(), db); 
    }

    public static boolean Initialize(Connection c) {
        _addColumn( _PrimaryKey = new CInteger("id").SetPrimaryKey(true) );
        _addColumn( new CVarchar("username").SetUnique(true) );
        _addColumn( new CVarchar("password") );
        _addColumn( new CVarchar("email", 255) );
        _addColumn( new CInteger("isAdmin") );
        _addColumn( new CInteger("isBanned") );

        User u = new User(c);
        return u.CreateTable(true);
    }

    public static SDWhereQuery<User> where(Connection c, String column, String comparison, String value) {
        return User.where(new User(c), column, comparison, value);
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ----------------------  Queries  -------------------------- 
    // --                                                       --
    // -----------------------------------------------------------

    public static User FindUser(String username) {

        return null;
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ------------------- Getters and Setters ------------------- 
    // --                                                       --
    // -----------------------------------------------------------

    public int getId() {
        return (int) values.get("id").Value;
    }

    public User setUsername(String value) {
        values.get("username").Value = value; return this;
    }

    public String getUsername() {
        return (String) values.get("username").Value;
    }

    public User setPassword(String value) {
        values.get("password").Value = value; return this;
    }

    public String getPassword() {
        return (String) values.get("password").Value;
    }

    public User setEmail(String Email) {
        values.get("Email").Value = Email; return this;
    }
    
    public String getEmail() {
        return (String) values.get("Email").Value;
    }

    public User setAdmin(int isAdmin) {
        values.get("isAdmin").Value = isAdmin; return this;
    }
    
    public int isAdmin() {
        return (int) values.get("isAdmin").Value;
    }

    public User setBanned(int banned) {
        values.get("banned").Value = banned; return this;
    }
    
    public int isBanned() {
        return (int) values.get("banned").Value;
    }
}