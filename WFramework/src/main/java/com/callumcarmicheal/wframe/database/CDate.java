package com.callumcarmicheal.wframe.database;

public class CDate extends DatabaseColumn<CDate> {
    public CDate(String name) { this.name = name; }

    @Override
    public String getColumnDefition() {
        // dateAdded DATE PRIMARY KEY AUTOINCREMENT
        return String.format("\"%s\" DATE%s", this.name, this.getColumnAttributes());
    }
} 