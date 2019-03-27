package com.callumcarmicheal.wframe.database.exceptions;

import com.callumcarmicheal.wframe.database.DatabaseColumn;
import com.callumcarmicheal.wframe.database.DatabaseColumnValue;

public class MissingColumnValueException extends Exception {
    private static final long serialVersionUID = 7922528697422705956L;
    private static DatabaseColumnValue columnValueStore;

    public MissingColumnValueException(DatabaseColumnValue databaseColumnValueStore) {
        super("The column " 
            + databaseColumnValueStore.getModel().orm_getTable() + "." + databaseColumnValueStore.Column.getName()
            + " cannot be null contains a null value");
        columnValueStore = databaseColumnValueStore;
    }
    
    public MissingColumnValueException(String message, DatabaseColumnValue databaseColumnValueStore) {
        super(message);
        columnValueStore = databaseColumnValueStore;
    }

    public DatabaseColumnValue getColumn() {
        return columnValueStore;
    }

}