package com.callumcarmicheal.wframe.database;

import com.callumcarmicheal.wframe.database.querybuilder.SDWhereQuery.QueryValueType;

public abstract class DatabaseColumn<T> {
    protected QueryValueType dataHandlingMethod = QueryValueType.Bound;

    protected boolean primaryKey = false;
    protected boolean nullable = true;
    protected boolean autoincrements = false;
    protected boolean unique  = false;

    protected String type = "";
    protected String name = "";
    protected Integer size = 0;

    public String getName() { return this.name; }
    public String getType() { return this.type; }
    
    public QueryValueType getHandlingMethod() { return this.dataHandlingMethod; }
    public T setHandlingMethod(QueryValueType b) { this.dataHandlingMethod = b; return (T)this; }

    public boolean getPrimaryKey() { return this.primaryKey; }
    public T setPrimaryKey(boolean b) {  this.nullable = false; this.primaryKey = b; return (T)this; }
    
    public boolean getAutoIncrement() { return this.autoincrements; }
    public T setAutoIncrements(boolean b) { this.autoincrements = b; return (T)this; }

    public boolean getNullable() { return this.nullable; }
    public T setNullable(boolean b) { this.nullable = b; return (T)this; }
    
    public boolean getUnique() { return this.unique; }
    public T setUnique(boolean b) {  this.unique = b; return (T)this; }
    
    public abstract String getColumnDefition();

    protected String getColumnAttributes() {
        return String.format("%s%s%s%s,", 
            this.nullable ? "" : " NOT NULL ",
            this.primaryKey ? " PRIMARY KEY " : "", 
            this.autoincrements ? " AUTOINCREMENT" : "",
            this.unique ? " UNIQUE " : "");
    }
}