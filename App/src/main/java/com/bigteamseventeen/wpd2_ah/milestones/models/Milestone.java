package com.bigteamseventeen.wpd2_ah.milestones.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import com.callumcarmicheal.wframe.database.*;
import com.callumcarmicheal.wframe.database.querybuilder.QueryResults;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery;
import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery.QueryValueType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Milestone extends DatabaseModel<Milestone> {
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
        return "Milestone";
    }

    /**
     * Create a instance of the user model
     */
    public Milestone(Connection c) { 
        // Setup the model instance settings
        super("milestones", _ColumnsDefinition, _PrimaryKey, c);
    }

    private static void _addColumn(DatabaseColumn db) {
        _ColumnsDefinition.put(db.getName(), db); 
    }

    public static boolean Initialize(Connection c) {
        _addColumn( _PrimaryKey = new CInteger("id").setPrimaryKey(true) );
        _addColumn( new CInteger("projectId") );
        _addColumn( new CVarchar("name", 250) );
        _addColumn( new CVarchar("description", 500) );
        _addColumn( new CDate("due") );
        _addColumn( new CDate("completed") );


        Milestone model = new Milestone(c);
        return model.CreateTable(true);
    }

    public static SDWhereQuery<Milestone> where(Connection connection, String column, String comparison, Object value) {
        return where(new Milestone(connection), column, comparison, value);
    }

    public static SDWhereQuery<Milestone> where(Connection connection, String column, String comparison, Object value, QueryValueType qvt) {
        return where(new Milestone(connection), column, comparison, value, qvt);
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ----------------------  Queries  -------------------------- 
    // --                                                       --
    // -----------------------------------------------------------

    public static Milestone[] All(Connection con) {
        return All(new Milestone(con));
    }

    public static Milestone Get(Connection connection, int id) {
        try {
            // Query the database
            QueryResults<Milestone> query = 
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

    public String getStrDueDate() {
        return "";
    }

    public String getStrCompletionDate() {
        return "-";
    }

    public boolean isIncomplete() {
        return false;
    }

    public boolean isOverdue() {
        return false;
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ------------------- Getters and Setters ------------------- 
    // --                                                       --
    // -----------------------------------------------------------
    public int getId() {
        return (int) values.get("id").Value;
    }

    public Milestone setProjectId(int projectId) {
        values.get("projectId").Value = projectId; return this;
    }
    
    public int getProjectId() {
        return (int) values.get("projectId").Value;
    }

    public Milestone setName(String name) {
        values.get("name").Value = name; return this;
    }
    
    public String getName() {
        return (String) values.get("name").Value;
    }

    public Milestone setDescription(String description) {
        values.get("description").Value = description; return this;
    }
    
    public String getDescription() {
        return (String) values.get("description").Value;
    }

    public Milestone setDueDate(String due) {
        values.get("due").Value = due; return this;
    }
    
    public String getDueDate() {
        return (String) values.get("due").Value;
    }

    public Milestone setCompetedOn(String completed) {
        values.get("completed").Value = completed; return this;
    }
    
    public String getCompetedOn() {
        return (String) values.get("completed").Value;
    }
}