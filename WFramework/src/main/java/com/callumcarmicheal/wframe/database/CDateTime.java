package com.callumcarmicheal.wframe.database;

public class CDateTime extends DatabaseColumn<CDate> {
    public CDateTime(String name) { this.name = name; }

    @Override
    public String getColumnDefition() {
        // dateChanged DATETIME PRIMARY KEY AUTOINCREMENT
        return String.format("\"%s\" DATETIME%s", this.name, this.getColumnAttributes());
    }
} 