package com.callumcarmicheal.wframe.web;

import java.io.Serializable;
import java.util.HashMap;

public class Session implements Serializable {
    private static final long serialVersionUID = 13421235453234L;
    protected HashMap<String, Serializable> sessionValues = new HashMap<>();

    private String sessionKey = null;

    public Session(String key) {
        this.sessionKey = key;
    }

    public <T extends Serializable> boolean isType(String key, Class<T> type) {
        if (this.sessionValues.containsKey(key))
            return false;

        Serializable value = this.sessionValues.get(key);
        return value.getClass().isInstance(type);
    }

    public <T extends Serializable> T get(String key, T defaultValue) {
        if (!this.sessionValues.containsKey(key)) {
            this.sessionValues.put(key, defaultValue);
            return defaultValue;
        }
        
        return (T)this.sessionValues.get(key);
    }

    public <T extends Serializable> T get(String key) {
        if (!this.sessionValues.containsKey(key))
            return null;
        
        return (T)this.sessionValues.get(key);
    }

    public void remove(String key) {
        this.sessionValues.remove(key);
    }

    public <T extends Serializable> boolean remove(String key, T object) {
        return this.sessionValues.remove(key, object);
    }

    public boolean containsKey(String key) {
        return this.sessionValues.containsKey(key);
    }

    public <T extends Serializable> boolean containsValue(T value) {
        return this.sessionValues.containsValue(value);
    }

    public <T extends Serializable> T set(String key, T value) {
        this.sessionValues.put(key, value);
        return value;
    }

    public String getSessionKey() {
        return this.sessionKey;
    }
}