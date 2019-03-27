package com.callumcarmicheal.wframe.database;

import com.callumcarmicheal.wframe.library.Tuple3;

@SuppressWarnings("rawtypes")
public class DatabaseColumnValue {
    public DatabaseColumn Column;
    public Object Value = null;

    protected DatabaseModel model;
    protected boolean isNullable = false;

    private Object originalValue = null;
    public Object getOriginalValue() {
        return originalValue;
    }

    /**
     * If the value has been modified
     */
    public boolean isModified() {
        return Value != originalValue;
    }

    /**
     * Check if the value is valid and can be inserted into the database
     * @return
     */
    public boolean isValid() {
        // If the value is not nullable and is null return false
        if (!this.isNullable && (this.Value == null))
            return false;
        

        // If we are not required then the it does not matter if the value has not been modified
        return true;
    }

    public DatabaseColumnValue(DatabaseModel Model, DatabaseColumn Column) {
        this(Model, Column, null);
    }

    public DatabaseColumnValue(DatabaseModel Model, DatabaseColumn Column, Object OriginalValue) {
        this.Column = Column;
        this.model = Model;
        originalValue = OriginalValue; 

        this.isNullable = Column.nullable;
    } 

    public DatabaseModel getModel() {
        return model;
    }

    @Override
    public String toString() {
        return this.Value.toString();
    }
}