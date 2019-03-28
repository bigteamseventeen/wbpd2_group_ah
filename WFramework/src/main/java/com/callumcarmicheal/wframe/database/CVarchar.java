package com.callumcarmicheal.wframe.database;

public class CVarchar extends DatabaseColumn<CVarchar> {
    public CVarchar(String name) { this(name, 128); }
    public CVarchar(String name, Integer size) {
        this.type = "varchar";
        this.size = size;
        this.name = name;
    }

    @Override
    public String getColumnDefition() {
        return String.format("\"%s\" VARCHAR(%d)%s", this.name, this.size, this.getColumnAttributes());
    }
}