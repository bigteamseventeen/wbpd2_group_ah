package com.bigteamseventeen.wpd2_ah.milestones.models;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    // -- --
    // ------------- Model Definition Attributes -----------------
    // -- --
    // -----------------------------------------------------------
    // Creating these for each instance would be taxing so we want to cache them
    private static LinkedHashMap<String, DatabaseColumn> _ColumnsDefinition = new LinkedHashMap<>();
    private static CInteger _PrimaryKey;

    @Override
    public String getModelName() {
        return "Milestone";
    }

    public Milestone(Connection c) {
        // Setup the model instance settings
        super("milestones", _ColumnsDefinition, _PrimaryKey, c);
    }

    /**
     * Create a instance of the model
     */
    public Milestone(Connection c, int plannerId) {
        this(c);
        setPlannerId(plannerId);
    }

    /**
     * Create a instance of the model
     */
    public Milestone(Connection c, Planner planner) {
        this(c, planner.getId());
    }

    private static void _addColumn(DatabaseColumn db) {
        _ColumnsDefinition.put(db.getName(), db);
    }

    public static boolean Initialize(Connection c) {
        _addColumn(_PrimaryKey = new CInteger("id").setPrimaryKey(true));
        _addColumn(new CInteger("planner"));
        _addColumn(new CVarchar("name", 250));
        _addColumn(new CVarchar("description", 500));
        _addColumn(new CDateTime("due"));
        _addColumn(new CDateTime("completed"));

        Milestone model = new Milestone(c);
        return model.CreateTable(true);
    }

    public static SDWhereQuery<Milestone> where(Connection connection, String column, String comparison, Object value) {
        return where(new Milestone(connection), column, comparison, value);
    }

    public static SDWhereQuery<Milestone> where(Connection connection, String column, String comparison, Object value,
            QueryValueType qvt) {
        return where(new Milestone(connection), column, comparison, value, qvt);
    }

    // -----------------------------------------------------------
    // -- --
    // ---------------------- Queries --------------------------
    // -- --
    // -----------------------------------------------------------

    public static Milestone[] All(Connection con) {
        return All(new Milestone(con));
    }

    public static Milestone Get(Connection connection, int id) {
        try {
            // Query the database
            QueryResults<Milestone> query = where(connection, "id", "=", id, QueryValueType.Bound).setLimit(1)
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
    // -- --
    // ----------------------- Operations ------------------------
    // -- --
    // -----------------------------------------------------------

    /**
     * Check if the milestone is incomplete
     * 
     * @return
     */
    public boolean isIncomplete() {
        return !this.isCompleted();
    }

    /**
     * Check if the milestone is overdue
     * 
     * @return
     */
    public boolean isOverdue() {
        // Check if we are completed, if so then compare to completion date
        if (this.isCompleted()) 
            return this.getDateDue().before(this.getDateCompleted());

        // Or we compare against todays date
        return this.getDateDue().before(new Date());
    }

    /**
     * Check if the milestone is completed
     * 
     * @return
     */
    public boolean isCompleted() {
        // Check if the string is not null and is not empty
        return this.getCompletedOn() != null && !this.getCompletedOn().isEmpty();
    }

    /**
     * Check if the user can modify this milestone
     * 
     * @param connection
     * @param user
     * @return
     */
    public boolean userCanEdit(Connection connection, User user) {
        // Get the planner
        Planner planner;
        if ((planner = getPlanner(connection)) == null)
            return false;

        // Check if author id == user id
        return planner.getAuthorId() == user.getId();
    }

    /**
     * Get the planner
     * 
     * @param connection
     * @return
     */
    public Planner getPlanner(Connection connection) {
        return Planner.Get(connection, getPlannerId());
    }

    /**
     * Get the due date as a Date object
     * 
     * @return
     */
    public Date getDateDue() {
        try {
            // Parse the string into a date
            return new SimpleDateFormat("dd/MM/yyyy").parse(this.getDueDate());
        } catch (ParseException e) {
            logger.error("Failed to format due date to Date object (getDateCompleted())", e);

            // Failed to format date object
            return null;
        }
    }

    /**
     * Get the completiond ate as a date object
     * 
     * @return
     */
    public Date getDateCompleted() {
        if (this.isCompleted()) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(this.getCompletedOn());
            } catch (ParseException e) {
                logger.error("Failed to format completion date to Date object (getDateCompleted())", e);

                // Failed to format date object
                return null;
            }
        }

        return null;
    }

    /**
     * Get the milestones status
     * @return
     */
    public String getMilestoneStatus() {
        return "";
    }

    /**
     * Generate a css class for the milestones
     * @return
     */
    public String getMilestoneStatusCSSClass() {
        // Completed            | Success
        // Overdue              | Danger
        // Completed && Overdue | Warning
        // Incomplete           | Secondary 

        if (this.isCompleted() && this.isOverdue()) 
            return "table-warning";
        
        if (this.isCompleted())     
            return "table-success";
        if (this.isOverdue())       
            return "table-danger";
        
        return "";
    }

    // -----------------------------------------------------------
    // --                                                       --
    // ------------------- Getters and Setters ------------------- 
    // --                                                       --
    // -----------------------------------------------------------
    public int getId() {
        return (int) values.get("id").Value;
    }

    public Milestone setPlannerId(int planner) {
        values.get("planner").Value = planner; return this;
    }
    
    public int getPlannerId() {
        return (int) values.get("planner").Value;
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

    public Milestone setCompletedOn(String completed) {
        values.get("completed").Value = completed; return this;
    }
    
    public String getCompletedOn() {
        return (String) values.get("completed").Value;
    }
}