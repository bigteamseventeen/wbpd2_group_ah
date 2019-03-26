package com.callumcarmicheal.wframe.database;

import com.callumcarmicheal.wframe.library.Tuple3;

@SuppressWarnings("rawtypes")
public class DatabaseColumnValue {
    public DatabaseColumn Column;
    public Object Value = null;
    protected boolean isRequired = false;

    private Object originalValue = null;
    public Object getOriginalValue() {
        return originalValue;
    }

    public boolean isModified() {
        return Value != originalValue;
    }

    public boolean isValid() {
        if (this.isRequired) {
            // TODO: Check if the value has been set and return accordingly
        }

        // If we are not required then the it does not matter if the value has not been modified
        return true;
    }

    public DatabaseColumnValue(DatabaseColumn Column) {
        this(Column, null);
    }

    public DatabaseColumnValue(DatabaseColumn Column, Object OriginalValue) {
        this.Column = Column;
        originalValue = OriginalValue; 

        this.isRequired = !Column.nullable;
    } 

    @Override
    public String toString() {
        return this.Value.toString();
    }
}