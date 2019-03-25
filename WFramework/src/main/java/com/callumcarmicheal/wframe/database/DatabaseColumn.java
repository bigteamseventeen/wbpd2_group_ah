package com.callumcarmicheal.wframe.database;

public abstract class DatabaseColumn<T> {
    protected boolean primaryKey = false;
    protected boolean nullable = true;
    protected boolean autoincrements = false;
    protected boolean unique  = false;

    protected String type = "";
    protected String name = "";
    protected Integer size = 0;

    public String GetName() { return this.name; }
    public String GetType() { return this.type; }
    
    public boolean GetPrimaryKey() { return this.primaryKey; }
    public T SetPrimaryKey(boolean b) {  this.nullable = false; this.primaryKey = b; return (T)this; }
    
    public boolean GetAutoIncrement() { return this.autoincrements; }
    public T SetAutoIncrements(boolean b) { this.autoincrements = b; return (T)this; }

    public boolean GetNullable() { return this.nullable; }
    public T SetNullable(boolean b) { this.nullable = b; return (T)this; }
    
    public boolean GetUnique() { return this.unique; }
    public T SetUnique(boolean b) {  this.unique = b; return (T)this; }
    
    public abstract String getColumnDefition();

    protected String getColumnAttributes() {
        return String.format("%s%s%s%s,", 
            this.nullable ? "" : " NOT NULL ",
            this.primaryKey ? " PRIMARY KEY " : "", 
            this.autoincrements ? " AUTOINCREMENT" : "",
            this.unique ? " UNIQUE " : "");
    }
}