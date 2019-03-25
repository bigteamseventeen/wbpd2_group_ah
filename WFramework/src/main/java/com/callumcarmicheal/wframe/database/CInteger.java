package com.callumcarmicheal.wframe.database;

public class CInteger extends DatabaseColumn<CInteger> {
    public CInteger(String name) { this.name = name; }

    @Override
    public String getColumnDefition() {
        // id INTEGER PRIMARY KEY AUTOINCREMENT
        return String.format("\"%s\" INTEGER%s", this.name, this.getColumnAttributes());
    }
} 