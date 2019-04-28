package com.bigteamseventeen.wpd2_ah.milestones.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.callumcarmicheal.wframe.database.*;
import com.callumcarmicheal.wframe.database.Helper.SQLOrderType;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery.QueryValueType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Planner extends DatabaseModel<Planner> {
    final static Logger logger = LogManager.getLogger();

    // -----------------------------------------------------------
    // --                                                       --
    // ------------- Model Definition Attributes -----------------
    // --                                                       --
    // -----------------------------------------------------------
    // Creating these for each instance would be taxing so we want to cache them
    private static LinkedHashMap<String, DatabaseColumn> _ColumnsDefinition = new LinkedHashMap<>();
    private static CInteger _PrimaryKey;

    @Override public String getModelName() {
        return "Planner";
    }

    /**
     * Create a instance of the user model
     */
    public Planner(Connection c) { 
        // Setup the model instance settings
        super("planner", _ColumnsDefinition, _PrimaryKey, c);
    }

    private static void _addColumn(DatabaseColumn db) {
        _ColumnsDefinition.put(db.getName(), db); 
    }

    public static boolean Initialize(Connection c) {
        // Set our columns
        _addColumn( _PrimaryKey = new CInteger("id").setPrimaryKey(true) );
        _addColumn( new CInteger("author") );
        _addColumn( new CVarchar("title") );
        _addColumn( new CVarchar("description", 250) );
        _addColumn( new CVarchar("share").setNullable(true).setUnique(true) );

        Planner model = new Planner(c);
        return model.CreateTable(true);
    }

    public static SDWhereQuery<Planner> where(Connection connection, String column, String comparison, Object value) {
        return where(new Planner(connection), column, comparison, value);
    }

    public static SDWhereQuery<Planner> where(Connection connection, String column, String comparison, Object value, QueryValueType qvt) {
        return where(new Planner(connection), column, comparison, value, qvt);
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ----------------------  Queries  -------------------------- 
    // --                                                       --
    // -----------------------------------------------------------

    public static Planner[] All(Connection con) {
        return All(new Planner(con));
    }

    public static Planner Get(Connection connection, int id) {
        try {
            // Query the database
            QueryResults<Planner> query = 
                where(connection, "id", "=", id, QueryValueType.Bound)
                    .setLimit(1)
                    .execute();

            if (query.Successful)
                return query.Rows[0];
        } catch (SQLException e) {
            logger.error("Failed to find milestone by id, SQL Exception", e);
            return null;
        } 

        return null;
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ----------------------- Operations ------------------------ 
    // --                                                       --
    // -----------------------------------------------------------

    private Milestone[] cache_Milestones = null;
    public Milestone[] milestones() {
        // Return the milestone cache
        return cache_Milestones;
    }

    public Milestone[] milestones(Connection connection) {
        try {
            QueryResults<Milestone> query = 
                Milestone.where(connection, "projectId", "=", this.getId())
                    .setOrderBy("due")
                    .setOrderByType(SQLOrderType.ASC)
                    .execute();

    
            // Get the milestones
            Milestone[] ms;
            if (query.Successful)
                ms = query.Rows;
            ms = new Milestone[0];
            
            // Cache the milestones
            this.cache_Milestones = ms;
            return ms;
        } catch(SQLException ex) {
            logger.error("Failed to load milestones from database (project_milestones_mtm).", ex);
            return new Milestone[0];
        }
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ------------------- Getters and Setters ------------------- 
    // --                                                       --
    // -----------------------------------------------------------

    public Planner setId(int id) {
        values.get("id").Value = id; return this;
    }
    
    public int getId() {
        return (int) values.get("id").Value;
    }

    public Planner setAuthorId(int author) {
        values.get("author").Value = author; return this;
    }
    
    public int getAuthorId() {
        return (int) values.get("author").Value;
    }

    public Planner setTitle(String title) {
        values.get("title").Value = title; return this;
    }
    
    public String getTitle() {
        return (String) values.get("title").Value;
    }

    public Planner setDescription(String description) {
        values.get("description").Value = description; return this;
    }
    
    public String getDescription() {
        return (String) values.get("description").Value;
    }

    public Planner setShareHash(String share) {
        values.get("share").Value = share; return this;
    }
    
    public String getShareHash() {
        return (String) values.get("share").Value;
    }
}