package com.callumcarmicheal.wframe.database;

import com.callumcarmicheal.wframe.library.Tuple3;

@SuppressWarnings("rawtypes")
public class DatabaseColumnValue {
    public DatabaseColumn Column;
    public Object Value = null;

    private Object originalValue;
    public Object getOriginalValue() {
        return originalValue;
    }

    public DatabaseColumnValue(DatabaseColumn Column) {
        this.Column = Column;
    }

    public DatabaseColumnValue(DatabaseColumn Column, Object OriginalValue) {
        this.Column = Column;
        originalValue = OriginalValue; 
    } 
}